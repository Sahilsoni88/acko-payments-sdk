# Payment SDK — Architecture

## High-Level Architecture

```mermaid
graph TD
    CS["Consumer Services\nClaims · Policy · Motor · Health · Endorsement"]

    subgraph SDK["Payment SDK"]
        API["Public API Layer\nPaymentClient\nPayoutOperations"]
        SVC["Application Services\nDefaultPayoutService"]
        INFRA["Infrastructure\nRequestExecutor · RetryExecutor\nExceptionMapper · MetricsHook"]
        AUTH["Auth Layer\nCookieHolder · TokenManager · TokenStore · AuthService"]
        HTTP["HTTP Client Layer\nPayoutFeignClient"]
    end

    AS["Auth Server\n(OAuth2 for payin/future surfaces)"]
    PAYOUT["Payout Platform\nPayoutServiceClient APIs"]
    PAYIN["Payin Platform\nPost-v0 target\n(orders · verify · refund)"]

    CS --> API
    API --> SVC
    SVC --> INFRA
    INFRA --> AUTH
    INFRA --> HTTP
    AUTH -. post-v0 .-> AS
    HTTP --> PAYOUT
    HTTP -. post-v0 .-> PAYIN
```

---

## Public API vs Platform Clients

In v0, the public API exposes payout only. Payin/refund is a post-v0 target surface.

| Public API | Feign client | Platform base URL | Status |
|---|---|---|---|
| `PayoutOperations` | `PayoutFeignClient` | `payment.payout.base-url` | Available in v0 |
| `PayinOperations` | `PayinFeignClient` | `payment.payin.base-url` | Post-v0 target |

Refund endpoints (`/refund/create`, `/refund/initiate`) will belong to **PayinOperations** / `PayinFeignClient` once payin ships.  
There is **no** `refund()` facade and **no** `payment.refund.base-url`.

```mermaid
graph LR
    PC["PaymentClient"]
    PO["payout()"]
    PI["payin() post-v0"]
    PFC["PayoutFeignClient"]
    PIFC["PayinFeignClient post-v0"]

    PC --> PO
    PC -. target .-> PI
    PO --> PFC
    PI -. target .-> PIFC
```

---

## Component Overview

### Public API Layer
- `PaymentClient` — single entry point
- `PayoutOperations` — only operation interface available in v0
- `PayinOperations` — post-v0 target for payin/refund
- Interfaces only; no HTTP/auth details exposed

### Application Services
- `DefaultPayoutService` in v0
- `DefaultPayinService` is post-v0 target design
- Map method → `RequestContext` + Feign call
- No auth/retry/logging logic inside services

### Infrastructure Layer
- `RequestExecutor` — full request lifecycle
- `RetryExecutor` — Spring Retry policy
- `ExceptionMapper` — Feign → SDK exceptions
- `MetricsHook` — pluggable metrics

### Auth Layer
- `CookieHolder`, `TokenManager`, `TokenStore`, `AuthService`
- Hidden from consumers; payout uses the configured Cookie header, while S2S bearer tokens are retained for payin/future surfaces

### HTTP Client Layer
- Package-private Feign clients only
- `PaymentFeignInterceptor` adds request-scoped auth headers: `Cookie` for payout, `Authorization` for S2S bearer-token surfaces
- v0 has `PayoutFeignClient`; post-v0 adds `PayinFeignClient`

---

## Endpoint Ownership

### PayoutFeignClient
| Method | Path |
|---|---|
| POST | `/api/v2/initiate_payout` |
| POST | `/api/initiate_payout/` |
| POST | `/api/v2/update_payout_details` |
| POST | `/api/validate/account_details` |
| GET | `/api/ifsc-verify?ifsc={ifsc}` |
| GET | `/api/{payout_request_id}/verify` |
| POST | `/api/generate_payout_request_id` |

### PayinFeignClient (post-v0 target)
| Method | Path |
|---|---|
| POST | `/payments/order-details-ekey` |
| GET | `/payments/{orderId}/verify` |
| GET | `/payments/{orderId}/verify/v2` |
| POST | `/refund/create` |
| POST | `/refund/initiate` |

---

## Key Design Decisions

**Why `payout()` now and `payin()` later?**  
That matches the platform ownership: `PayoutServiceClient` is available in v0, while `PayinServiceClient` is the target home for payin and refund APIs. Refunds will stay on `payin()` when that surface ships.

**Why only platform-owned Feign clients?**  
Same reason — accurate ownership, less config, fewer timeouts to tune. v0 has `PayoutFeignClient`; post-v0 adds `PayinFeignClient`.

**Why core Feign instead of Spring Cloud OpenFeign?**  
Programmatic setup, per-client timeouts, no classpath scanning, no Spring Cloud dependency.

**Why thread-local for token propagation?**  
Keeps Feign interfaces clean. Executor sets token, interceptor reads it, executor clears it in `finally`.

**Why RestTemplate for auth, Feign for payment APIs?**  
Auth is one form-encoded token endpoint; RestTemplate avoids a form-encoder dependency for that single call.

**Why package-private implementations?**  
Small, intentional public surface. Consumers cannot couple to internals.

---

## Configuration Flow

```mermaid
flowchart TD
    YML["application.yml\nauth · payout · defaults\npayin post-v0"]
    PROPS["PaymentProperties"]
    RES["ConfigResolver"]
    CLIENT["PayoutFeignClient\n+ RetryTemplate"]
    FUTURE["PayinFeignClient post-v0"]

    YML --> PROPS --> RES --> CLIENT
    RES -. target .-> FUTURE
```

---

## Dependency Flow

```mermaid
graph TD
    PC["PaymentClient"]
    DPC["DefaultPaymentClient"]
    DPS["DefaultPayoutService"]
    PFC["PayoutFeignClient"]
    DPAS["DefaultPayinService post-v0"]
    PIFC["PayinFeignClient post-v0"]
    RE["RequestExecutor"]
    TM["TokenManager"]
    TS["TokenStore"]
    AS["AuthService"]
    RTE["RetryExecutor"]
    EM["ExceptionMapper"]
    MH["MetricsHook"]

    PC --> DPC
    DPC --> DPS
    DPC -. target .-> DPAS
    DPS --> PFC
    DPS --> RE
    DPAS -. target .-> PIFC
    DPAS -. target .-> RE
    RE --> TM & RTE & EM & MH
    TM --> TS & AS
```
