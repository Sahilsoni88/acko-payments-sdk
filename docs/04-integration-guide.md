# Payment SDK — Integration Guide

This guide shows how a consumer service should use the SDK. Start here.

**v0 (`0.1.0`):** only `paymentClient.payout()` is available. Payin/refund sections below describe the target API and will apply once those surfaces ship.

---

## Mental Model

The SDK exposes **one entry point**: `PaymentClient`.

Under it, operations map to the Central Payment Platform clients:

| SDK surface | Platform client | Responsibility | v0 |
|---|---|---|---|
| `paymentClient.payout()` | `PayoutServiceClient` | Disbursals, account/IFSC validation, payout verify | Available |
| `paymentClient.payin()` | `PayinServiceClient` | Collect money, verify, and refunds | Upcoming |

There is **no** separate `refund()` entry. Refund APIs are part of `payin()`.

Consumers should **only** inject `PaymentClient`. Never depend on Feign clients, token managers, or HTTP details.

---

## 1. Add the Dependency

```xml
<dependency>
    <groupId>com.acko</groupId>
    <artifactId>acko-payments-sdk-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

Resolve the artifact from Acko Nexus.

---

## 2. Configure `application.yml`

For v0, configure payout base URL and the internal payout cookie. S2S bearer-token auth is for payin/future surfaces, not payout.

```yaml
payment:
  defaults:
    timeout:
      connect: 2s
      read: 5s
    retry:
      enabled: true
      max-attempts: 3
      backoff: 500ms

  payout:
    base-url: https://payout.payments.internal
    cookie-header: ${INTERNAL_PAYOUT_COOKIE}
    timeout:
      read: 10s
```

> `INTERNAL_PAYOUT_COOKIE` should be the raw Cookie header value, for example `internalPayoutCookie=...`. Never hardcode it; load it from env or secrets manager.

---

## 3. Auth model

Payout uses the configured Cookie header directly and does not fetch an OAuth token. S2S bearer-token support and the in-memory `TokenStore` remain available for payin/future surfaces.

Non-Spring consumers:

```java
PaymentClient client = PaymentClientFactory.create(sdkConfig);
```

---

## 4. Recommended Usage Flows

### 4.1 Payout (disburse money) — available in v0

Typical claim / refund-to-bank / settlement flow:

```text
[optional] generatePayoutRequestId
     → verifyIfsc
     → validateAccountDetails
     → initiatePayout
     → [optional] updatePayoutDetails
     → verify (sync)  and/or  SQS event (async)
```

V0 payout request objects intentionally mirror Central Payment Platform field names. For example,
`InitiatePayoutRequest` uses `okind`, `oid`, `paymentType`, `paymentMode`, `uniqueId`, and
`BigDecimal amount` instead of a higher-level SDK-specific payout command object.

```java
@Service
@RequiredArgsConstructor
public class ClaimPayoutService {

    private final PaymentClient paymentClient;

    public String disburse(DisbursalCommand cmd) {
        // 1) Pre-flight checks (recommended before initiate)
        paymentClient.payout().verifyIfsc(cmd.getIfscCode());

        paymentClient.payout().validateAccountDetails(
            ValidateAccountDetailsRequest.builder()
                .accountNumber(cmd.getAccountNumber())
                .ifscCode(cmd.getIfscCode())
                .accountHolderName(cmd.getBeneficiaryName())
                .accountType("bank")
                .build()
        );

        // 2) Generate platform request id when your flow requires it
        GeneratePayoutRequestIdResponse idResponse =
            paymentClient.payout().generatePayoutRequestId();

        // 3) Initiate — persist payout_request_id before/at this call
        InitiatePayoutResponse response = paymentClient.payout().initiate(
            InitiatePayoutRequest.builder()
                .okind("jarvis")
                .oid(cmd.getClaimId())
                .paymentType("claim")
                .amount(cmd.getAmount())
                .requestedById(cmd.getRequestedById())
                .entityType("customer")
                .entityId(cmd.getCustomerId())
                .callbackUrl(cmd.getCallbackUrl())
                .paymentMode("bank")
                .payoutRequestId(idResponse.getPayoutRequestId())
                .uniqueId(cmd.getIdempotencyKey()) // optional, if platform supports
                .paymentInstrument(PaymentInstrument.builder()
                    .accountNumber(cmd.getAccountNumber())
                    .ifscCode(cmd.getIfscCode())
                    .accountHolderName(cmd.getBeneficiaryName())
                    .build())
                .build()
        );

        // response.getId() is payout-service result.id. It is different from payout_request_id.
        // Keep payout_request_id for verify/status recovery.
        return idResponse.getPayoutRequestId();
    }
}
```

**Update after initiate** (e.g. corrected beneficiary details, when platform allows). The update `id` is the numeric payout/payment id from payout-service state, not the string `payout_request_id`:

```java
paymentClient.payout().updatePayoutDetails(
    UpdatePayoutDetailsRequest.builder()
        .id(paymentNumericId)
        .amount(updatedAmount)
        .paymentInstrument(updatedInstrument)
        .requestedById(requestedById)
        .paymentMode("bank")
        .callbackUrl(callbackUrl)
        .build()
);
```

**Sync status check** (timeout recovery / UI confirmation):

```java
VerifyPayoutResponse status =
    paymentClient.payout().verify(payoutRequestId);

if (status.getPaymentStatus() == PaymentStatus.COMPLETED) {
    // proceed
}
```

---

### 4.2 Payin (collect money) — upcoming (post-v0)

```text
createOrder (order-details-ekey)
     → redirect / open payment experience
     → verify  or  verifyV2
     → SQS event for final confirmation (recommended)
```

```java
CreateOrderResponse order = paymentClient.payin().createOrder(
    CreateOrderRequest.builder()
        .correlationId(correlationId)
        .amount(Money.ofInr(premium))
        .customerId(customerId)
        .referenceId(policyId)
        .callbackUrl("https://your-service/payments/callback")
        .build()
);

// Send user to order.getPaymentUrl() / use returned ekey payload as platform requires
```

After gateway callback (or when you need immediate confirmation):

```java
// Prefer v2 when your product is onboarded to verify/v2
VerifyPayinResponse result = paymentClient.payin().verifyV2(orderId);

// Or legacy verify
VerifyPayinResponse legacy = paymentClient.payin().verify(orderId);
```

> Prefer **async SQS events** as source of truth for final success. Use `verify` / `verifyV2` for timeout recovery and callback confirmation.

---

### 4.3 Refund (under payin) — upcoming (post-v0)

Refunds are a two-step flow on the same Payin client:

```text
payin().createRefund  →  payin().initiateRefund  →  SQS event
```

```java
CreateRefundResponse created = paymentClient.payin().createRefund(
    CreateRefundRequest.builder()
        .correlationId(correlationId)
        .orderId(orderId)
        .amount(Money.ofInr(refundAmount))
        .reason("Policy cancelled")
        .build()
);

InitiateRefundResponse initiated = paymentClient.payin().initiateRefund(
    InitiateRefundRequest.builder()
        .correlationId(correlationId)
        .refundId(created.getRefundId())
        .build()
);
```

---

## 5. Handle Exceptions

```java
try {
    InitiatePayoutResponse response = paymentClient.payout().initiate(request);
    // For the legacy route, use:
    // InitiatePayoutResponse response = paymentClient.payout().initiateV1(request);
} catch (ValidationException e) {
    // 4xx — fix request, do not retry blindly
} catch (AuthenticationException e) {
    // auth / token failure — alert ops
} catch (PaymentTimeoutException e) {
    // confirm outcome with verify
    VerifyPayoutResponse status =
        paymentClient.payout().verify(payoutRequestId);
} catch (DownstreamException e) {
    // retries exhausted or unexpected platform error
} catch (PaymentException e) {
    // catch-all
}
```

---

## 6. Sync Status vs Async Events

| Need | Use |
|---|---|
| Immediate check after timeout / callback | `payout().verify(...)` / `payin().verify(...)` / `verifyV2(...)` |
| Final business state transition | Subscribe to payment SQS events (see `05-event-contract.md`) |

Do both when reliability matters: verify for recovery, events for the main workflow.

---

## 7. Custom Metrics (Optional)

```java
@Bean
public MetricsHook paymentMetricsHook(MeterRegistry registry) {
    return new MicrometerMetricsHook(registry);
}
```

---

## 8. What NOT to Do

| ❌ Don't | ✅ Do instead |
|---|---|
| Call platform URLs / Feign clients directly | Use `PaymentClient` only |
| Inject `TokenManager` for payout | Configure `payment.payout.cookie-header` |
| Treat callback alone as final success | Confirm with `verify`/`verifyV2` and/or SQS |
| Skip account/IFSC checks before payout | Call `verifyIfsc` + `validateAccountDetails` |
| Expect `paymentClient.refund()` | Use `paymentClient.payin().createRefund` / `initiateRefund` |
| Create refund without initiate | Follow `createRefund` → `initiateRefund` |
| Log tokens / account numbers in clear text | Log masked / safe identifiers only |
| Configure a separate `payment.refund.base-url` | Refunds use `payment.payin.base-url` |

---

## 9. Minimal Checklist for a New Integrator (v0)

1. Add SDK dependency (`0.1.0`)  
2. Configure `payment.payout.base-url` and `payment.payout.cookie-header`  
3. Inject `PaymentClient` (or use `PaymentClientFactory` without Spring)  
4. Implement payout end-to-end (validate → generate id → initiate → verify on timeout)  
5. Add exception handling + timeout → `verify` recovery (never blind-retry initiate)  
6. Subscribe to SQS events for final status  
7. Add `MetricsHook` bean (optional)
