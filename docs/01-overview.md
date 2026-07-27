# Payment SDK — Overview

**Current release line:** `0.1.1-SNAPSHOT` (v0) — **payout only**. Payin and refund APIs are planned for later minor releases.

## What is it?

The Payment SDK is an internal Java library that provides a single, standardized way to integrate with the Central Payment Platform.

Instead of each service implementing its own authentication, retry, timeout, and error-handling logic, services depend on this SDK and get all of that for free.

---

## Why does it exist?

Before the SDK, every team that needed to process payments had to:

- Handle service authentication independently
- Write their own retry and timeout logic
- Handle HTTP errors inconsistently
- Maintain boilerplate integration code against payout/payin endpoints

This led to duplicated code, bugs in edge cases, and high maintenance cost. The SDK centralizes that in one place.

---

## Platform surface (source of truth)

The SDK wraps two platform HTTP clients:

### PayoutServiceClient
- `POST /api/v2/initiate_payout`
- `POST /api/initiate_payout/`
- `POST /api/v2/update_payout_details`
- `POST /api/validate/account_details`
- `GET /api/ifsc-verify?ifsc={ifsc}`
- `GET /api/{payout_request_id}/verify`
- `POST /api/generate_payout_request_id`

### PayinServiceClient (post-v0)
- `POST /payments/order-details-ekey`
- `GET /payments/{orderId}/verify`
- `GET /payments/{orderId}/verify/v2`
- `POST /refund/create`
- `POST /refund/initiate`

---

## What it does

| Capability | v0 | Description |
|---|---|---|
| **Authentication** | Yes | Payout uses `Cookie: internalPayoutCookie=...`; S2S bearer-token support is retained for payin/future surfaces |
| **Payout** | Yes | Generate request id, IFSC/account validation, initiate, update, verify |
| **Payin** | Later | Create order (ekey), verify, verify v2 |
| **Refund** | Later | Create + initiate via `payin()` (same PayinServiceClient) |
| **Retry** | Yes | Configurable retry on transient failures (5xx, timeouts); no blind retry on initiate |
| **Timeout** | Yes | Configurable connect/read timeouts per service |
| **Error Mapping** | Yes | HTTP errors mapped to typed SDK exceptions |
| **Observability** | Yes | Structured logs with correlation ID, latency; pluggable metrics hook |

---

## What it does NOT do

- Store or persist payment data
- Manage business-level payment workflows / sagas
- Own SQS listeners for payment events
- Poll asynchronously for status
- Replace the Central Payment Platform

---

## Who should use it?

Any internal Java Spring Boot service that needs to interact with the Central Payment Platform (Claims, Policy, Motor, Health, Endorsement, etc.).

---

## How to start

1. Read **[04-integration-guide.md](./04-integration-guide.md)** — how to use the SDK  
2. Read **[06-design-recommendations.md](./06-design-recommendations.md)** — recommended public API shape  
3. Use **[02-api-contracts.md](./02-api-contracts.md)** for endpoint mapping  

---

## Tech Stack

- Java 21
- Framework-neutral core + optional Spring Boot 3.x auto-configuration
- OpenFeign core (HTTP client)
- Payout Cookie auth
- OAuth2 Client Credentials support for S2S bearer-token surfaces

## Repository Modules

```
acko-payments-sdk/
├── acko-payments-sdk-core     # published SDK artifact: com.acko:acko-payments-sdk
└── acko-payments-sdk-example  # local Spring Boot manual-test app, not deployed
```
