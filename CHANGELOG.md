# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0-SNAPSHOT] - Unreleased

### Added

- Framework-neutral core with optional Spring Boot auto-configuration
- OAuth2 client-credentials auth with in-memory token cache and refresh buffer
- Request pipeline: token propagation, retry on transient failures, timeout, exception mapping
- `PaymentClient.payout()` with:
  - `generatePayoutRequestId`
  - `verifyIfsc`
  - `validateAccountDetails`
  - `initiate`
  - `updatePayoutDetails`
  - `verify`
- `PaymentClientFactory` for non-Spring consumers
- Maven `dev` / `prod` Nexus deploy profiles
- Unit and Spring smoke tests

### Notes

- Payin and refund APIs are deferred to post-v0 releases (`0.2.0` / `0.3.0`).
- Deploy snapshot to Acko Nexus (dev): `mvn clean deploy -Pdev` (requires `~/.m2/settings.xml` server ids `dev-snapshots` / `release`).
