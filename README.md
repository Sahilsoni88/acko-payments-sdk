# Acko Payments SDK

Internal Java library for calling the Central Payment Platform from Acko services.

One entry point — `PaymentClient` — with payout cookie auth, retries, timeouts, and typed errors. Consumers should not wire Feign clients themselves.

| | |
|---|---|
| **Artifact** | `com.acko:acko-payments-sdk-core:0.1.0` |
| **Java** | 21+ |
| **Spring** | Optional (Boot 3.x auto-config) |
| **v0 scope** | Payout only (`paymentClient.payout()`) |

Payin and refund ship in later releases. Full consumer guide: [docs/04-integration-guide.md](docs/04-integration-guide.md).

---

## Why this SDK exists

Teams that need payout/payin historically each reimplemented:

- Payout cookie headers / service authentication
- Retry / timeout behavior
- Platform HTTP error handling

The SDK centralizes that so Claims, Policy, Motor, Health, and others share one integration path.

---

## Quick start (Spring Boot)

### 1. Dependency

```xml
<dependency>
    <groupId>com.acko</groupId>
    <artifactId>acko-payments-sdk-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

Resolve the artifact from Acko Nexus.

### 2. Configuration

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
```

`INTERNAL_PAYOUT_COOKIE` should be the raw Cookie header value, for example `internalPayoutCookie=...`. Never hardcode secrets — use env or secrets manager.

### 3. Use `PaymentClient`

```java
@Service
@RequiredArgsConstructor
public class ClaimPayoutService {

    private final PaymentClient paymentClient;

    public String disburse(DisbursalCommand cmd) {
        paymentClient.payout().verifyIfsc(cmd.getIfscCode());

        paymentClient.payout().validateAccountDetails(
            ValidateAccountDetailsRequest.builder()
                .accountNumber(cmd.getAccountNumber())
                .ifscCode(cmd.getIfscCode())
                .accountHolderName(cmd.getBeneficiaryName())
                .accountType("bank")
                .build());

        GeneratePayoutRequestIdResponse idResponse =
            paymentClient.payout().generatePayoutRequestId();

        // Persist payout_request_id before/at initiate
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
                .paymentInstrument(PaymentInstrument.builder()
                    .accountNumber(cmd.getAccountNumber())
                    .ifscCode(cmd.getIfscCode())
                    .accountHolderName(cmd.getBeneficiaryName())
                    .build())
                .build());

        return idResponse.getPayoutRequestId();
    }
}
```

### Timeout recovery

On timeout for money-moving calls, **verify** — do not blind-retry `initiate`:

```java
try {
    paymentClient.payout().initiate(request);
} catch (PaymentTimeoutException e) {
    VerifyPayoutResponse status =
        paymentClient.payout().verify(payoutRequestId);
}
```

---

## Non-Spring usage

```java
SdkConfig config = SdkConfig.builder()
    .payout(new ServiceSettings(
        payoutBaseUrl,
        null,
        null,
        "internalPayoutCookie=" + internalPayoutCookie))
    .build();

PaymentClient client = PaymentClientFactory.create(config);
client.payout().verify(payoutRequestId);
```

---

## Public API (v0)

```text
PaymentClient
└── payout()  → PayoutOperations
      ├── generatePayoutRequestId()
      ├── verifyIfsc(ifsc)
      ├── validateAccountDetails(request)
      ├── initiate(request)
      ├── initiateV1(request)
      ├── updatePayoutDetails(request)
      └── verify(payoutRequestId)
```

| Method | Platform path |
|---|---|
| `generatePayoutRequestId` | `POST /api/generate_payout_request_id` |
| `verifyIfsc` | `GET /api/ifsc-verify` |
| `validateAccountDetails` | `POST /api/validate/account_details` |
| `initiate` | `POST /api/v2/initiate_payout` |
| `initiateV1` | `POST /api/initiate_payout/` |
| `updatePayoutDetails` | `POST /api/v2/update_payout_details` |
| `verify` | `GET /api/{payout_request_id}/verify` |

---

## What the SDK does / does not do

| Does | Does not |
|---|---|
| Payout Cookie header propagation | Persist payment state |
| Payout HTTP calls via Feign | Own business workflows / sagas |
| Configurable retry & timeouts | SQS / SNS listeners |
| Typed exceptions + safe logging | Async status polling |
| Spring auto-config (optional) | Replace the Central Payment Platform |

---

## Build & test

```bash
mvn clean test
mvn -pl acko-payments-sdk-core clean package
```

Core packages must stay Spring-free:

```bash
rg "org\\.springframework" acko-payments-sdk-core/src/main/java/com/acko/payment/sdk --glob '!**/spring/**'
```

---

## Example app

Use `acko-payments-sdk-example` for local/manual payout testing. The published SDK artifact still comes only from `acko-payments-sdk-core`; the example module is marked `maven.deploy.skip=true`.

```bash
cp .env.example .env
# edit .env with the actual Cookie header value
set -a
source .env
set +a
mvn -pl acko-payments-sdk-example spring-boot:run
```

Spring Boot/Maven do not automatically load `.env`; source it before running the example app.

Example endpoints:

| Method | Path |
|---|---|
| GET | `/api/test/paymentsdk/payout/ifsc?ifsc=SBIN0017118` |
| POST | `/api/test/paymentsdk/payout/request-id` |
| POST | `/api/test/paymentsdk/payout/validate-account` |
| POST | `/api/test/paymentsdk/payout/initiate-v2` |
| POST | `/api/test/paymentsdk/payout/initiate-v1` |
| POST | `/api/test/paymentsdk/payout/update` |
| GET | `/api/test/paymentsdk/payout/{payoutRequestId}/verify` |

Apidog/Postman import files:

| File | Purpose |
|---|---|
| [`docs/apidog-payout-example.postman_collection.json`](docs/apidog-payout-example.postman_collection.json) | Clean endpoint collection for manual calls |
Apidog imports Postman collections as endpoint cases. To run them together, create a Test Scenario in Apidog, then add steps from the imported endpoint cases in the required order.

---

## Deploy

Maven profiles write to Acko Nexus. Configure `~/.m2/settings.xml` with servers `release`, `dev-snapshots`, and `prod-snapshots`.

```bash
mvn -pl acko-payments-sdk-core clean deploy -Pdev    # snapshots / release candidates
mvn -pl acko-payments-sdk-core clean deploy -Pprod   # production releases only
```

Branching and SemVer rules: [.cursor/rules/payment-sdk.md](.cursor/rules/payment-sdk.md).

---

## Documentation

| Doc | Purpose |
|---|---|
| [docs/04-integration-guide.md](docs/04-integration-guide.md) | Consumer integration (start here) |
| [docs/01-overview.md](docs/01-overview.md) | Product overview & scope |
| [docs/02-api-contracts.md](docs/02-api-contracts.md) | Platform request/response contracts |
| [docs/03-lld.md](docs/03-lld.md) | Package layout & request pipeline |
| [docs/architecture.md](docs/architecture.md) | High-level architecture |
| [docs/05-event-contract.md](docs/05-event-contract.md) | Async events (owned by consumers) |
| [docs/06-design-recommendations.md](docs/06-design-recommendations.md) | Public API design choices |
| [CHANGELOG.md](CHANGELOG.md) | Release notes |

---

## Roadmap

| Version | Focus |
|---|---|
| `0.1.x` (v0) | Payout + auth/retry/timeout/errors |
| `0.2.0` | Payin `createOrder` + `verify` / `verifyV2` |
| `0.3.0` | Refund `create` → `initiate` under `payin()` |
| `1.0.0` | GA after pilot + polish |

---

## License

Internal Acko use only.
