# Payment SDK — Integration Guide

This guide shows how a consumer service should use the SDK. Start here.

---

## Mental Model

The SDK exposes **one entry point**: `PaymentClient`.

Under it, operations map to the Central Payment Platform clients:

| SDK surface | Platform client | Responsibility |
|---|---|---|
| `paymentClient.payout()` | `PayoutServiceClient` | Disbursals, account/IFSC validation, payout verify |
| `paymentClient.payin()` | `PayinServiceClient` | Collect money, verify, and refunds |

There is **no** separate `refund()` entry. Refund APIs are part of `payin()`.

Consumers should **only** inject `PaymentClient`. Never depend on Feign clients, token managers, or HTTP details.

---

## 1. Add the Dependency

```xml
<dependency>
    <groupId>com.acko</groupId>
    <artifactId>acko-payments-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 2. Configure `application.yml`

Only two payment base URLs are required (plus auth):

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

  auth:
    base-url: https://auth.payments.internal
    client-id: ${PAYMENT_CLIENT_ID}
    client-secret: ${PAYMENT_CLIENT_SECRET}
    scope: payment.write

  payout:
    base-url: https://payout.payments.internal
    timeout:
      read: 10s

  payin:
    base-url: https://payin.payments.internal
    # refund endpoints also use this base-url

  token-cache:
    refresh-buffer: 30s
    cache-key: acko-payment-sdk:oauth-token
```

> Never hardcode secrets. Load `client-id` / `client-secret` from env or secrets manager.

---

## 3. Provide the HybridCache Bean

```java
@Bean
public HybridCache<String, OAuthToken> paymentTokenCache(
        AckoHybridCache orgCache) {
    return new AckoHybridCache<>(orgCache, "payment-tokens");
}
```

---

## 4. Recommended Usage Flows

### 4.1 Payout (disburse money)

Typical claim / refund-to-bank / settlement flow:

```text
[optional] generatePayoutRequestId
     → verifyIfsc
     → validateAccountDetails
     → initiatePayout
     → [optional] updatePayoutDetails
     → verify (sync)  and/or  SQS event (async)
```

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
                .ifsc(cmd.getIfscCode())
                .beneficiaryName(cmd.getBeneficiaryName())
                .correlationId(cmd.getCorrelationId())
                .build()
        );

        // 2) Generate platform request id when your flow requires it
        GeneratePayoutRequestIdResponse idResponse =
            paymentClient.payout().generatePayoutRequestId(
                GeneratePayoutRequestIdRequest.builder()
                    .correlationId(cmd.getCorrelationId())
                    .referenceId(cmd.getClaimId())
                    .build()
            );

        // 3) Initiate
        InitiatePayoutResponse response = paymentClient.payout().initiate(
            InitiatePayoutRequest.builder()
                .correlationId(cmd.getCorrelationId())
                .payoutRequestId(idResponse.getPayoutRequestId())
                .amount(Money.ofInr(cmd.getAmount()))
                .beneficiaryAccountNumber(cmd.getAccountNumber())
                .beneficiaryIfscCode(cmd.getIfscCode())
                .beneficiaryName(cmd.getBeneficiaryName())
                .mode(PaymentMode.NEFT)
                .referenceId(cmd.getClaimId())
                .build()
        );

        return response.getPayoutRequestId();
    }
}
```

**Update after initiate** (e.g. corrected beneficiary details, when platform allows):

```java
paymentClient.payout().updatePayoutDetails(
    UpdatePayoutDetailsRequest.builder()
        .payoutRequestId(payoutRequestId)
        .correlationId(correlationId)
        // fields allowed by platform update contract
        .build()
);
```

**Sync status check** (timeout recovery / UI confirmation):

```java
VerifyPayoutResponse status =
    paymentClient.payout().verify(payoutRequestId);

if (status.getStatus() == PaymentStatus.SUCCESS) {
    // proceed
}
```

---

### 4.2 Payin (collect money)

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

### 4.3 Refund (under payin)

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
| Inject `TokenManager` | SDK owns token lifecycle |
| Treat callback alone as final success | Confirm with `verify`/`verifyV2` and/or SQS |
| Skip account/IFSC checks before payout | Call `verifyIfsc` + `validateAccountDetails` |
| Expect `paymentClient.refund()` | Use `paymentClient.payin().createRefund` / `initiateRefund` |
| Create refund without initiate | Follow `createRefund` → `initiateRefund` |
| Log tokens / account numbers in clear text | Log masked / safe identifiers only |
| Configure a separate `payment.refund.base-url` | Refunds use `payment.payin.base-url` |

---

## 9. Minimal Checklist for a New Integrator

1. Add SDK dependency  
2. Configure `auth`, `payout`, `payin`  
3. Provide `HybridCache` bean  
4. Inject `PaymentClient`  
5. Implement one flow (payout **or** payin) end-to-end  
6. Add exception handling + timeout → verify recovery  
7. Subscribe to SQS events for final status  
8. Add metrics hook (optional)
