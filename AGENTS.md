# AGENTS.md

## Project

Order Platform is a learning sandbox for practicing backend development through an order-processing service. Favor correctness, clarity, maintainability, testability, and reviewable changes over production-scale complexity.

Stack: Java 25, Spring Boot, Gradle, PostgreSQL, Flyway, OAuth 2.0, SNS/SQS through LocalStack, JUnit 5, Mockito, AssertJ, Testcontainers, and Docker Compose. Add frameworks, dependencies, infrastructure, or architectural patterns only for a concrete requirement, and explain the need and trade-offs.

## Architecture

This is a single-module **Pragmatic Layered Architecture with Decoupled Domain**:

- `domain.model`: entities, value objects, and invariants; `domain.service`: use-case coordination; `domain.port`: meaningful external-boundary contracts.
- `web`: controllers, HTTP DTOs, and error handling.
- `persistence`: JPA entities, repositories, persistence operations, and infrastructure adapters such as ID generation.
- `messaging`: broker-facing listeners, messages, and publishers.
- `configuration`: Spring wiring and runtime configuration.

Rules:

- Domain code must remain independent from Spring, JPA, web DTOs, persistence entities, and infrastructure representations.
- HTTP DTOs, persistence entities, and domain models are separate representations; map them explicitly rather than reusing them for convenience.
- Services coordinate use cases; controllers may call them directly. Add a separate application layer only for a concrete need.
- Introduce interfaces at meaningful external boundaries when they improve isolation or testability. Do not add or remove ports, adapters, interactors, repository interfaces, or other indirection merely to match an architectural style.

## Domain and Data

- Keep business invariants in domain objects; services coordinate behavior spanning objects or external capabilities.
- Prefer immutable types, meaningful value objects, constructor injection, and standard-library solutions.
- Generate UUIDv7 identifiers in the application.
- Represent money with `BigDecimal` and ISO 4217 currency at scale 4, without implicit rounding.
- Use `Instant` for creation events and `LocalDate` for business dates; inject `Clock` for time-dependent behavior.
- Flyway exclusively owns schema changes; Hibernate only validates. Consider transaction, network, and messaging failures explicitly.

## Implementation

Inspect the code and tests relevant to the task. Make the smallest coherent change, preserve existing conventions, and avoid unrelated refactors or changes to APIs, schemas, dependencies, or boundaries.

Use modern, idiomatic Java: small cohesive classes, records for immutable carriers, and `var` when inference is obvious. Avoid field injection, unnecessary inheritance, speculative abstractions, and comments that restate code. Explain significant architectural choices.

## Testing Strategy

Use the smallest useful scope and test observable behavior rather than implementation details:

- **Services:** focused, fast, deterministic unit tests without Spring. Mock external collaborators, not domain objects. Cover meaningful behavior, rules, errors, and branches with JUnit 5, Mockito, and AssertJ.
- **Controllers/APIs:** application integration tests covering `HTTP -> controller -> service -> persistence -> PostgreSQL`. Use the appropriate Spring context, Testcontainers, real PostgreSQL, and Flyway; do not mock services or persistence. Verify contracts, validation, errors, and relevant state without duplicating service tests.
- **Messaging:** use unit tests for mapping and coordination; reserve LocalStack integration tests for broker topology and behavior that an in-memory substitute would not prove.
- **Persistence:** ordinarily covered through application integration tests. Add dedicated integration tests only for custom queries, important constraints, non-trivial mappings, locking/concurrency, or transaction behavior. Do not test simple JPA/framework behavior for coverage.
- **General:** name container-backed tests `*IntegrationTest`; `unitTest` excludes that pattern. Add regression coverage when practical. Do not start Spring for pure unit tests, duplicate assertions without added value, or create artificial tests solely to reach a coverage percentage.

## Verification

- During implementation, run the narrowest relevant tests and expand the scope when the change crosses boundaries.
- The pre-commit hook runs `./gradlew unitTest`. Run it directly before handoff when no commit is requested; do not repeat it if it already passed against unchanged code.
- The pre-push hook and CI run `./gradlew check`. Run it directly before handing off code, configuration, or schema changes when Docker is available; the LocalStack topology test also requires `LOCALSTACK_AUTH_TOKEN`.
- For documentation-only changes, `git diff --check` is sufficient unless the documentation changes executable commands or configuration.
- Report commands executed, failures, and skipped tests.
- Do not claim unverified behavior works.
