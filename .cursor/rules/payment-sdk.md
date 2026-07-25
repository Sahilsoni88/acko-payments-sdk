
---
description: Payment SDK development, testing, and deployment rules

globs:
  - "**/*.java"
  - "pom.xml"
  - "docs/**/*.md"

alwaysApply: true

---


# Payment SDK Development, Testing, and Deployment Rules

These rules are mandatory for `acko-payments-sdk`. The SDK must keep a small public surface, use SemVer releases, deploy through Acko Nexus dev/prod profiles, minimize framework coupling, and keep docs in sync with behavior.

## Read First

Before making any change, read all documentation under `docs/`.

Documentation is the source of truth for:
- SDK scope and non-goals
- Public API shape
- Platform endpoint mapping
- Configuration contract
- Event and error contracts
- Integration guidance

If implementation and docs disagree, stop and fix the docs or raise the mismatch before changing behavior.

## Stay in Scope

Only implement what is requested.

Do not:
- Refactor unrelated code
- Introduce new frameworks
- Change public APIs unless the task explicitly requires it
- Add new platform endpoints unless they are documented in `docs/02-api-contracts.md`
- Add persistence, queues, schedulers, workflow orchestration, or business-level payment state
- Make consumer services depend on internal HTTP clients, token managers, or SDK internals

The SDK must stay focused on payment-platform integration concerns: authentication, token caching, HTTP calls, retries, timeouts, error mapping, and consumer-friendly request/response models.

## Java and Spring Compatibility

This SDK must be as Java-version and Spring-version independent as practical.

Follow these rules for every dependency and code change:
- Prefer the lowest reasonable Java baseline for the SDK; do not use Java 21-only language/runtime features unless the SDK version explicitly raises the baseline.
- Keep public APIs free of Spring-specific types wherever possible.
- Keep DTOs, enums, exceptions, and core client contracts framework-neutral.
- Isolate Spring Boot auto-configuration, configuration properties, and bean wiring from core SDK logic.
- Treat Spring integration as an adapter layer, not the SDK core.
- Prefer standard Java, Jackson, Feign core, SLF4J API, and small SPI-style interfaces over Spring-only abstractions.
- Mark compile-time helpers as `optional` or `provided` where appropriate, especially Lombok and annotation processors.
- Consumers must provide logging bindings; the SDK should depend on `slf4j-api`, not force Logback/Log4j in main scope.
- Do not force a Spring Boot BOM upgrade on consumers unless it is part of a planned SDK release and documented as a compatibility change.
- Test against the supported Java/Spring matrix before release whenever a change touches auto-configuration, dependency management, HTTP wiring, or serialization.

## Spring Dependency Enforcement

The SDK must be designed as a framework-neutral core with an optional Spring integration layer.

Core SDK code must include:
- `PaymentClient` and public operation interfaces
- Request/response DTOs
- Enums and value objects
- SDK exceptions and error models
- Authentication, token, retry, timeout, and HTTP abstractions
- Feign/client construction logic that can run without a Spring application context

Core SDK code must not include:
- `org.springframework.*` imports
- `org.springframework.boot.*` imports
- `@Component`, `@Service`, `@Configuration`, `@Bean`, `@Autowired`, or similar Spring annotations
- `@ConfigurationProperties` or Spring Boot property binding
- Spring `ApplicationContext`, `Environment`, `RestTemplate`, `WebClient`, `RetryTemplate`, or Spring Security types in public APIs
- Spring Boot starters in main dependency scope

Spring-specific code must live only in a clearly isolated integration layer, such as a dedicated Spring adapter/starter package or module.

Spring integration may include:
- Auto-configuration
- Configuration properties
- Bean creation and conditional wiring
- Spring Security OAuth adapters
- Spring Retry adapters
- Spring-specific token cache adapters
- Spring Boot metadata generation

Spring integration rules:
- Spring dependencies must not leak into core SDK packages or public DTOs.
- Spring Boot starters are allowed only in the Spring integration layer, not in core SDK code.
- `spring-boot-configuration-processor` must remain optional.
- Optional integration dependencies must be marked `optional` where consumers should not be forced to include them.
- Lombok must remain `optional` or `provided`; it must not be required at runtime.
- Logging implementations must remain test/runtime consumer choices; only `slf4j-api` belongs in main SDK dependencies.
- If this repository stays single-module, package boundaries must still enforce the same separation: core packages must remain Spring-free and Spring classes must stay under a clearly named adapter/autoconfigure package.
- If Spring dependencies start leaking into core behavior, split the SDK into separate core and Spring integration artifacts before release.

Enforcement checks before merging Spring/dependency changes:
- Run `mvn dependency:tree` and verify no unexpected Spring Boot starter or logging binding is pulled into main SDK scope.
- Search core packages for Spring imports before merge.
- Verify the SDK can be used by a non-Spring Java consumer through manually constructed clients or factories.
- Verify Spring auto-configuration remains additive: consumers can override beans and properties without depending on SDK internals.

Compatibility expectations:
- A patch release must not raise Java or Spring requirements.
- A minor release may add support for newer Java/Spring versions while preserving existing support.
- A major release is required if the SDK drops support for a previously supported Java or Spring baseline.

## Dependency Rules

Keep the SDK lightweight for consumers.

Allowed by default:
- Java standard library
- Feign core modules needed by the SDK
- Jackson modules needed for platform payloads
- SLF4J API
- Jakarta validation API
- Spring libraries only inside the Spring integration layer

Avoid:
- Spring Boot starters in main scope unless absolutely required
- Spring types in public APIs, DTOs, exceptions, or core client contracts
- Concrete logging implementations in main scope
- Heavy transitive dependency trees
- Consumer-service dependencies leaking into the SDK
- Multiple libraries solving the same concern

Before adding or upgrading a dependency:
- Check transitive dependencies with `mvn dependency:tree`
- Confirm it does not pin consumers to a narrow Spring Boot version
- Confirm it does not raise the Java baseline accidentally
- Document the reason if it affects consumers

## Branching Strategy

Branch names must include the target SDK version.

Use SemVer: `MAJOR.MINOR.PATCH[-SNAPSHOT]`.

Branch types:
- `main`: stable release history only
- `develop/sdk-vX.Y.Z-SNAPSHOT`: active development for the next SDK version
- `feature/sdk-vX.Y.Z/<short-description>`: feature work for the target SDK version
- `bugfix/sdk-vX.Y.Z/<short-description>`: backward-compatible bug fixes
- `release/sdk-vX.Y.Z`: release hardening branch for a production SDK version
- `hotfix/sdk-vX.Y.Z/<short-description>`: urgent production patch from `main`

Examples:
- `develop/sdk-v1.1.0-SNAPSHOT`
- `feature/sdk-v1.1.0/add-refund-status-mapping`
- `bugfix/sdk-v1.0.1/token-refresh-race`
- `release/sdk-v1.1.0`
- `hotfix/sdk-v1.0.2/timeout-error-mapping`

Branch rules:
- Feature and bugfix branches must be cut from the matching `develop/sdk-vX.Y.Z-SNAPSHOT` branch.
- Release branches must be cut from `develop` only after version, docs, tests, and changelog are ready.
- Hotfix branches must be cut from `main`, merged back to `main`, and then merged/cherry-picked into active development.
- Do not merge changes for one SDK version into another version branch without explicitly updating docs and compatibility notes.
- Release tags must match the SDK artifact version exactly: `vX.Y.Z`.

## Versioning Rules

Use SemVer for the Maven artifact version.

Version meanings:
- `MAJOR`: breaking public API, behavior, Java baseline, Spring baseline, configuration, or dependency compatibility change
- `MINOR`: backward-compatible endpoint support, options, models, or adapter features
- `PATCH`: backward-compatible bug fixes, documentation fixes, dependency patches, or non-breaking internals
- `-SNAPSHOT`: active development build only

Rules:
- Development branches use `X.Y.Z-SNAPSHOT`.
- Release branches and tags use `X.Y.Z`.
- Production releases must not use `-SNAPSHOT`.
- After a production release, immediately move the next development branch/version to the next `-SNAPSHOT`.
- Public API additions require documentation updates in the same change.
- Public API removals, renamed fields, changed defaults, changed exception types, or changed retry/timeout behavior require a major version unless explicitly proven backward compatible.

## Build and Test Commands

Run commands from the repository root.

Minimum local verification before a PR:

```bash
mvn clean test
mvn clean package
```

Recommended verification for dependency or compatibility changes:

```bash
mvn clean install
mvn dependency:tree
mvn javadoc:javadoc
rg "org\\.springframework|springframework|SpringBoot|ConfigurationProperties|ApplicationContext|RetryTemplate|RestTemplate|WebClient" src/main/java
```

Focused test runs are allowed while developing:

```bash
mvn test -Dtest=ClassNameTest
mvn test -Dtest=ClassNameTest#methodName
```

Testing expectations:
- Every feature or bug fix must include appropriate tests.
- Cover happy paths, validation failures, HTTP failures, retry behavior, timeout behavior, token refresh behavior, and serialization edge cases where relevant.
- Add integration or Spring-context tests when auto-configuration, bean creation, configuration binding, or conditional wiring changes.
- Do not require external payment-platform connectivity in unit tests.
- Use mocks/fakes for platform HTTP clients and auth providers unless a test is explicitly marked as integration.
- Public DTOs and exceptions should have serialization/deserialization and backward-compatibility coverage when changed.

## Deployment Commands

The SDK must deploy through Maven `dev` and `prod` profiles backed by Acko Nexus repositories.

Expected Maven profile names:
- `dev`: deploys to Acko dev Nexus
- `prod`: deploys to Acko production Nexus

Required Nexus server IDs in `~/.m2/settings.xml`:
- `release`
- `dev-snapshots`
- `prod-snapshots`

Deploy snapshot or release candidate to dev first:

```bash
mvn clean deploy -Pdev
```

Deploy production release only after release checklist passes:

```bash
mvn clean deploy -Pprod
```

Useful version management commands:

```bash
mvn versions:set -DnewVersion=X.Y.Z-SNAPSHOT
mvn versions:set -DnewVersion=X.Y.Z
mvn versions:commit
mvn versions:revert
```

Deployment repository expectations:
- Dev releases: `http://nexus-dev.acko.in:8080/repository/maven-releases/`
- Dev snapshots: `http://nexus-dev.acko.in:8080/repository/maven-snapshots/`
- Prod releases: `https://nexus-prod.acko.in/repository/maven-releases/`
- Prod snapshots: `https://nexus-prod.acko.in/repository/maven-snapshots/`

## Deployment Checklist

Before deploying to dev:
- Version ends with `-SNAPSHOT` unless intentionally testing a release candidate.
- `mvn clean test` passes.
- `mvn clean package` passes.
- `mvn dependency:tree` has no unexpected Spring Boot, logging, or Java-baseline drift.
- Relevant docs under `docs/` are updated.
- Consumer-impacting changes are documented.
- Artifact resolves locally with `mvn clean install`.
- Deploy with `mvn clean deploy -Pdev`.

Before deploying to prod:
- Release branch name is `release/sdk-vX.Y.Z`.
- Maven artifact version is exactly `X.Y.Z`.
- Version does not contain `-SNAPSHOT`.
- Dev artifact has already been tested in at least one consuming service or sample app.
- Full test suite passes with `mvn clean test`.
- Package/install passes with `mvn clean install`.
- Javadocs generate with `mvn javadoc:javadoc`.
- Public docs and integration guide are updated.
- Compatibility impact is documented.
- Release notes/changelog are updated if present.
- Git tag is prepared as `vX.Y.Z`.
- Deploy with `mvn clean deploy -Pprod`.
- Push tag after successful production deploy.
- Move active development to the next `X.Y.Z-SNAPSHOT`.

## Documentation Rules

If a change affects any of the following, update docs in the same change:
- Public APIs
- Request/response models
- Configuration keys or defaults
- Error codes or exception mapping
- Retry/timeout behavior
- Token caching behavior
- Supported Java/Spring versions
- Maven dependency or repository instructions
- Deployment process
- Payment platform endpoint mapping

Never leave documentation outdated.

## Code Quality

Follow:
- SOLID principles
- Constructor injection
- Clean Architecture
- Package-by-feature where it fits the existing structure
- Clear public API names
- Typed exceptions for SDK failures
- Correlation IDs and safe structured logging

Do not log:
- Client secrets
- OAuth tokens
- PAN/account numbers in full
- Raw payment payloads containing sensitive data
- Personally identifiable information unless explicitly masked

Prefer reusable components over duplicated code, but avoid broad abstractions until at least two real SDK use cases need them.

## Before Completing a Task

Ensure:
- Changes remain within scope.
- Code compiles when code is changed.
- Tests pass or any skipped test is explicitly explained.
- Documentation is updated.
- Java/Spring compatibility has not regressed.
- Public API changes are versioned correctly.
- Deployment or release-impacting changes include the required commands/checklist updates.
