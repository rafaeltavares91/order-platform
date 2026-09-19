# AGENTS.md

## Project

Order Platform is a production-oriented learning project. Favor correctness, clarity, maintainability, testability, and reviewable changes. Add complexity only for a concrete requirement.

Stack: Java 25, Spring Boot, Gradle, PostgreSQL, Flyway, JUnit 5, Mockito, AssertJ, Testcontainers, and Docker Compose. Do not add frameworks, dependencies, infrastructure, or architectural patterns without explaining the need and trade-offs. Kafka, outbox, sagas, CQRS, Redis, and observability infrastructure remain deferred until required.

## Architecture

This is a single-module **Pragmatic Layered Architecture with Domain Separation**:

- `domain.model`: entities, value objects, and invariants; `domain.service`: use-case coordination; `domain.port`: useful external-boundary contracts.
- `web`: controllers, DTOs, and errors; `persistence`: JPA entities, repositories, persistence operations, and infrastructure adapters such as ID generation; `configuration`: Spring wiring.

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

Inspect relevant code and tests first. Make the smallest coherent change, preserve existing conventions, and avoid unrelated refactors or changes to APIs, schemas, dependencies, or boundaries.

Use modern, idiomatic Java: small cohesive classes, records for immutable carriers, and `var` when inference is obvious. Avoid field injection, unnecessary inheritance, speculative abstractions, and comments that restate code. Explain significant architectural choices.

## Testing Strategy

Use the smallest useful scope and test observable behavior rather than implementation details:

- **Services:** focused, fast, deterministic unit tests without Spring. Mock external collaborators, not domain objects. Cover meaningful behavior, rules, errors, and branches with JUnit 5, Mockito, and AssertJ.
- **Controllers/APIs:** application integration tests covering `HTTP -> controller -> service -> persistence -> PostgreSQL`. Use the appropriate Spring context, Testcontainers, real PostgreSQL, and Flyway; do not mock services or persistence. Verify contracts, validation, errors, and relevant state without duplicating service tests.
- **Persistence:** ordinarily covered through application integration tests. Add dedicated integration tests only for custom queries, important constraints, non-trivial mappings, locking/concurrency, or transaction behavior. Do not test simple JPA/framework behavior for coverage.
- **General:** add regression coverage when practical. Do not start Spring for pure unit tests, duplicate assertions without added value, or create artificial tests solely to reach a coverage percentage.

## Verification

- Compile and run relevant tests; run `./gradlew test` when practical.
- Report commands executed, failures, and skipped tests.
- Do not claim unverified behavior works.
