# Payment SDK — Meeting Brief

**Audience:** Tech Lead · Manager  
**Goal:** Align on what we build, what it covers, and what stays out of scope  
**Status:** Design proposed · Implementation not started / early

---

## 1. One-liner

An internal **Java Spring Boot SDK** that gives every consumer service one standard way to call the Central Payment Platform — without each team re-implementing auth, retries, timeouts, and error handling.

---

## 2. Problem we are solving

Today, teams that need payout / payin / refund (Claims, Policy, Motor, Health, Endorsement, etc.) each build their own integration:

| Pain | Impact |
|---|---|
| Duplicate OAuth2 token logic | Bugs, token thrash, inconsistent refresh |
| Ad-hoc retry / timeout | Double payout risk, flaky failures |
| Inconsistent error handling | Hard to debug across services |
| Direct coupling to platform HTTP | Every platform change fans out to many repos |

**Outcome we want:** one library, one mental model, safer money-moving defaults.

---

## 3. What we are thinking to do

### Approach
- Ship a **library** (not a new microservice).
- Thin, intentional public API that **mirrors the platform**:
  - `paymentClient.payout()` → `PayoutServiceClient`
  - `paymentClient.payin()` → `PayinServiceClient` (orders, verify, **refunds**)
- SDK owns cross-cutting concerns: auth, retry, timeout, exception mapping, logging/metrics.
- Consumers own business workflows, persistence, and SQS listeners.

### Public API shape (proposed)

```text
PaymentClient
├── payout()
│     generatePayoutRequestId · verifyIfsc · validateAccountDetails
│     initiate · updatePayoutDetails · verify
└── payin()
      createOrder · verify · verifyV2
      createRefund · initiateRefund
```

**Key design choice:** No top-level `refund()`. Refunds are payin platform APIs → they live under `payin()`.

### Sync vs async model

| Concern | Owner | Mechanism |
|---|---|---|
| Call platform APIs | SDK | Sync HTTP (Feign) |
| Recover from timeout / ambiguity | SDK + consumer | Sync `verify` / `verifyV2` |
| Final payment state / workflow | Consumer | SNS → SQS events |

**Rule of thumb:** events drive completion; verify APIs recover from uncertainty.

### Tech stack
Java 21 · Spring Boot 3.x · OpenFeign · Spring Retry · OAuth2 Client Credentials · HybridCache (token)

### Suggested delivery phases

| Phase | Focus | Why first |
|---|---|---|
| **P0** | Payout happy path + `verify` recovery | Highest misuse risk (double payout) |
| **P1** | Payin `createOrder` + `verifyV2` | Core collection path |
| **P2** | Refund `create` → `initiate` | Completes money lifecycle |
| **P3** | Validation helpers, metrics polish | Safety + operability |

---

## 4. Capabilities (what the SDK will provide)

### Platform operations

**Payout**
- Generate payout request id  
- IFSC verify + account details validation  
- Initiate payout / update payout details  
- Sync verify by `payout_request_id`

**Payin**
- Create order (ekey)  
- Verify (v1 legacy) + Verify v2 (preferred for new products)

**Refund** (via `payin()`)
- Create refund → initiate refund  
- No dedicated refund verify API today → rely on events

### Cross-cutting (the real value of the SDK)

| Capability | Behavior |
|---|---|
| Auth | OAuth2 client credentials, token cache + refresh — hidden from consumers |
| Retry | Configurable; only for safe/transient failures |
| Timeout | Connect/read timeouts; overridable per payout/payin |
| Errors | HTTP → typed SDK exceptions |
| Observability | Structured logs (correlation id, latency, request id); pluggable metrics |
| Config | `payment.auth` + `payment.payout` + `payment.payin` (+ defaults) |
| DX | Single bean to inject: `PaymentClient` |

### Integrator guidance we will document
- Validate before payout initiate  
- Persist platform ids (`payout_request_id` / `order_id` / refund id) + `reference_id` before/at mutate  
- On timeout → **verify**, do not blind-retry initiate  
- New payin products use `verifyV2`  
- Refund is always **create then initiate**

---

## 5. What we will NOT cover

Explicit non-goals (important for scope control):

| Out of scope | Why |
|---|---|
| Storing / persisting payment state | Consumer DB ownership |
| Business workflows / sagas | Product-specific |
| SQS / SNS listeners inside the SDK | Events stay in consumer services |
| Async status polling loops | Keeps SDK stateless |
| Replacing Central Payment Platform | We wrap it; we don’t own money movement |
| Separate `refund()` facade or `payment.refund.base-url` | Platform has only payout + payin clients |
| UI / payment gateway UX | Outside library boundary |
| Non-Java clients (Node, Go, etc.) | Java/Spring first |
| Owning event schema as source of truth | Owned with platform team |

---

## 6. Things easy to miss (call out in the meeting)

These are gaps / decisions that should be resolved early — not always obvious from the API list.

### Open dependencies on Platform
1. **Request/response field schemas** — paths are agreed; payload schemas still need finalization.  
2. **Event envelope schema** — planned, TBD with Central Payment Platform.  
3. **Idempotency guarantees** — especially for `initiate` payout / `createOrder` / refund. If platform supports `idempotency_key`, SDK should expose and propagate it.  
4. **Refund verify** — no sync verify today; confirm events are sufficient for all consumers.

### Product / engineering decisions to confirm
5. **Pilot consumer** — which service adopts first (e.g. Claims payout or Policy payin)?  
6. **Migration path** — how existing ad-hoc Feign/Rest clients move to SDK (big-bang vs coexistence).  
7. **Versioning & release** — Maven artifact coordinates, semver policy, breaking-change rules.  
8. **Ownership** — who owns SDK releases, reviews, and on-call for SDK bugs vs platform outages.  
9. **Secrets** — standard for `client-id` / `client-secret` (env / secrets manager); no hardcoded secrets.  
10. **Success metrics** — e.g. % services on SDK, reduction in duplicate payout incidents, p95 integration time.

### Safety rules we should socialize
11. **Timeout ≠ failure** for money APIs — verify before retry.  
12. **Callback ≠ final truth** for payin — confirm via verify and/or event.  
13. **Correlation** — every mutating call should carry `correlation_id` + consumer `reference_id`.

### Rollout / support (often skipped in design reviews)
14. Integration guide + example app / sample config for adopters.  
15. Compatibility matrix (Spring Boot / Java versions we support).  
16. Changelog + upgrade notes from day one.

---

## 7. Decisions we need from this meeting

| # | Decision | Options / recommendation |
|---|---|---|
| D1 | Approve public API shape (`payout` / `payin` only)? | **Recommend: yes** |
| D2 | Refund under `payin()` (not top-level)? | **Recommend: yes** |
| D3 | First pilot consumer? | Need nomination |
| D4 | P0 delivery focus? | **Recommend: payout + verify recovery** |
| D5 | Who owns event schema finalization with platform? | Need owner |
| D6 | Idempotency key support — block P0 or phase later? | Prefer confirm with platform soon |

---

## 8. How this fits architecturally (30-second picture)

```text
Consumer Services (Claims · Policy · Motor · Health · Endorsement)
        │
        ▼
   PaymentClient  (SDK public API)
     ├── payout()  → Payout platform
     └── payin()   → Payin platform (incl. refund)
        │
        ├── Auth (OAuth2, cached)
        ├── Retry / Timeout / Error mapping
        └── Logs / Metrics
        │
        ▼
Central Payment Platform ──► SNS → SQS ──► Consumer listeners (NOT in SDK)
```

---

## 9. Appendix — quick reference

**In scope methods (target):**  
Payout: `generatePayoutRequestId`, `verifyIfsc`, `validateAccountDetails`, `initiate`, `updatePayoutDetails`, `verify`  
Payin: `createOrder`, `verify`, `verifyV2`, `createRefund`, `initiateRefund`

**Detailed docs (if deeper dive needed):**  
`01-overview` · `02-api-contracts` · `03-lld` · `04-integration-guide` · `05-event-contract` · `06-design-recommendations` · `architecture.md`
