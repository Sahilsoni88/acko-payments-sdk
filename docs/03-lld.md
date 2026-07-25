# Payment SDK — Low-Level Design (LLD)

## Package Structure

```
com.acko.payment.sdk
├── api/            → PaymentClient, PayoutOperations, PayinOperations
├── auth/           → TokenManager, TokenStore, AuthService, OAuthToken
├── cache/          → HybridCache adapter interface
├── common/         → RequestExecutor, RetryExecutor, ExceptionMapper, FeignInterceptor
├── config/         → PaymentProperties, ConfigResolver, PaymentAutoConfiguration
├── events/         → Optional hooks / constants related to payment events (no SQS listeners)
├── exception/      → SDK exception hierarchy
├── model/          → Shared value objects (Money, PaymentStatus, PaymentMode)
├── payin/          → Payin + refund models, PayinFeignClient, DefaultPayinService
├── payout/         → Payout models, PayoutFeignClient, DefaultPayoutService
└── util/           → MaskingUtils, CorrelationIdHolder
```

> Refund request/response models live under `payin/` and are exposed through `PayinOperations`.

---

## Layer Responsibilities

| Layer | Classes | Rule |
|---|---|---|
| **Public API** | `PaymentClient`, `PayoutOperations`, `PayinOperations` | Interface only |
| **Service** | `DefaultPayoutService`, `DefaultPayinService` | API → `RequestContext` + Feign call |
| **Infrastructure** | `RequestExecutor`, `RetryExecutor`, `ExceptionMapper` | Cross-cutting concerns |
| **Auth** | `TokenManager`, `TokenStore`, `AuthService` | Token lifecycle only |
| **Feign** | `PayoutFeignClient`, `PayinFeignClient` | HTTP mapping only; package-private |
| **Config** | `PaymentProperties`, `ConfigResolver` | Hierarchical config |

---

## Public Operations (target API)

### `PayoutOperations`
- `generatePayoutRequestId(GeneratePayoutRequestIdRequest) → GeneratePayoutRequestIdResponse`
- `verifyIfsc(String ifsc) → VerifyIfscResponse`
- `validateAccountDetails(ValidateAccountDetailsRequest) → ValidateAccountDetailsResponse`
- `initiate(InitiatePayoutRequest) → InitiatePayoutResponse`
- `updatePayoutDetails(UpdatePayoutDetailsRequest) → UpdatePayoutDetailsResponse`
- `verify(String payoutRequestId) → VerifyPayoutResponse`

### `PayinOperations`
- `createOrder(CreateOrderRequest) → CreateOrderResponse`
- `verify(String orderId) → VerifyPayinResponse`
- `verifyV2(String orderId) → VerifyPayinResponse`
- `createRefund(CreateRefundRequest) → CreateRefundResponse`
- `initiateRefund(InitiateRefundRequest) → InitiateRefundResponse`

---

## Feign Mapping

### `PayoutFeignClient`
| Operation | HTTP | Path |
|---|---|---|
| initiate | POST | `/api/v2/initiate_payout` |
| updatePayoutDetails | POST | `/api/v2/update_payout_details` |
| validateAccountDetails | POST | `/api/validate/account_details` |
| verifyIfsc | GET | `/api/ifsc-verify` |
| verify | GET | `/api/{payout_request_id}/verify` |
| generatePayoutRequestId | POST | `/api/generate_payout_request_id` |

### `PayinFeignClient`
| Operation | HTTP | Path |
|---|---|---|
| createOrder | POST | `/payments/order-details-ekey` |
| verify | GET | `/payments/{orderId}/verify` |
| verifyV2 | GET | `/payments/{orderId}/verify/v2` |
| createRefund | POST | `/refund/create` |
| initiateRefund | POST | `/refund/initiate` |

---

## Request Execution Flow

```mermaid
sequenceDiagram
    participant C as Consumer
    participant PS as DefaultPayoutService
    participant RE as RequestExecutor
    participant TM as TokenManager
    participant TS as TokenStore
    participant AS as AuthService
    participant RT as RetryExecutor
    participant FC as PayoutFeignClient
    participant CPP as Payout Platform

    C->>PS: initiate(request)
    PS->>RE: execute(context, feignCall)
    RE->>TM: getValidToken()
    TM->>TS: get(cacheKey)

    alt Cache Hit
        TS-->>TM: OAuthToken
    else Cache Miss
        TM->>AS: fetchToken()
        AS-->>TM: OAuthToken
        TM->>TS: store(key, token, ttl)
    end

    TM-->>RE: accessToken
    Note over RE: set token in ThreadLocal
    RE->>RT: execute(context, feignCall)
    RT->>FC: initiate(request)
    Note over FC: Interceptor adds Authorization
    FC->>CPP: POST /api/v2/initiate_payout
    CPP-->>FC: InitiatePayoutResponse
    FC-->>RT: response
    RT-->>RE: response
    Note over RE: clear ThreadLocal · record metrics
    RE-->>PS: response
    PS-->>C: InitiatePayoutResponse
```

Refund create/initiate follows the same path via `DefaultPayinService` → `PayinFeignClient` → `/refund/*`.

---

## Token Management Flow

```mermaid
flowchart TD
    A([getValidToken called]) --> B{Cached token exists\nand not near expiry?}
    B -- Yes --> C([Return cached token])
    B -- No --> D[Acquire ReentrantLock]
    D --> E{Re-check after lock}
    E -- Valid --> F([Return cached token])
    E -- Still expired/missing --> G["AuthService.fetchToken()"]
    G --> H["TokenStore.store(key, token, ttl)"]
    H --> I([Return new token])
    D --> J[Release lock in finally]
```

`refreshBuffer` (default 30s) refreshes before expiry. Lock prevents thundering herd.

---

## Retry Policy

- Spring Retry `RetryTemplate`
- Retry on `RetryableException` (5xx, network, timeouts)
- No retry on 4xx
- Configurable: `max-attempts`, `backoff`, `enabled`

---

## Exception Hierarchy

```mermaid
classDiagram
    class PaymentException {
        +String errorCode
        +String message
    }
    class AuthenticationException
    class ValidationException
    class RetryableException
    class PaymentTimeoutException
    class DownstreamException

    PaymentException <|-- AuthenticationException
    PaymentException <|-- ValidationException
    PaymentException <|-- RetryableException
    PaymentException <|-- PaymentTimeoutException
    PaymentException <|-- DownstreamException
```

`RetryableException` is internal; after retries exhaust it becomes `DownstreamException`.

---

## Configuration Precedence

```
payment.payout.timeout.read = 10s   ← highest (service-level)
payment.defaults.timeout.read = 5s  ← fallback
SDK hardcoded default = 5s          ← lowest
```

Refund calls use **`payment.payin`** timeouts/retry (same Feign client).

---

## Thread Safety

| Concern | Mechanism |
|---|---|
| Token refresh | `ReentrantLock` + double-checked locking |
| Token → Feign | `ThreadLocal`, cleared in `finally` |
| Concurrent calls | Per-thread `ThreadLocal` slot |

---

## Visibility Rules

| Class Type | Visibility |
|---|---|
| `PaymentClient`, `PayoutOperations`, `PayinOperations`, request/response models, exceptions | `public` |
| Default services, Feign clients, token internals | `package-private` |

---

## Auto-Configuration

```mermaid
graph TD
    ACI["META-INF/spring/\nAutoConfiguration.imports"]
    PAC["PaymentAutoConfiguration"]
    AC["AuthConfiguration"]
    CC["CommonConfiguration"]
    POC["PayoutConfiguration"]
    PIC["PayinConfiguration"]

    ACI --> PAC
    PAC --> AC & CC & POC & PIC
```

No `@Enable*` annotation required. Consumer adds dependency + config only.
