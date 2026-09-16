# AGENTS.md

## Project

Order Platform is a production-oriented learning project for order processing. Favor correctness, clarity, and reviewable changes. Add complexity only for a concrete requirement.

Core stack: Java 25, Spring Boot, Gradle, PostgreSQL, Flyway, JUnit 5, Mockito, AssertJ, Testcontainers, and Docker Compose.

Do not add frameworks, dependencies, infrastructure, or architectural patterns without explaining the need and trade-offs. Kafka, outbox, sagas, CQRS, Redis, and observability infrastructure remain deferred until required.

## Architecture

This is a single-module, pragmatic hexagonal architecture:

- `domain.model`: entities, value objects, and business invariants.
- `domain.service`: use-case coordination.
- `domain.port`: contracts for external capabilities.
- `web`: HTTP controllers, DTOs, and error handling.
- `persistence`: JPA entities, repositories, and adapters.
- `identifier`: identifier generation.
- `configuration`: Spring wiring.

The domain is the center:

- Domain code must not depend on Spring, JPA, web DTOs, persistence entities, or infrastructure details.
- Domain services may depend on domain ports; infrastructure implements those ports.
- Controllers may call domain services directly. Do not add interfaces solely for layering symmetry.
- External representations such as HTTP DTOs, persistence entities, and future event/message schemas are separate from domain models. Do not reuse domain objects as transport or persistence models for convenience.
## Domain and Data Rules

- Keep business rules and invariants in domain objects; services coordinate behavior that does not belong to one object.
- Prefer immutable types, meaningful value objects, constructor injection, and standard-library solutions.
- Use application-generated UUIDv7 identifiers.
- Represent money with `BigDecimal` and ISO 4217 currency using scale 4, with no implicit rounding.
- Use `Instant` for creation events and `LocalDate` for business dates. Inject `Clock` into time-dependent behavior.
- Flyway exclusively owns schema changes. Hibernate validates the schema; it must not create or update it.
- Consider transaction, network, and messaging failure modes explicitly.

## Implementation

Before changing code, inspect the relevant implementation and tests. Make the smallest coherent change and extend existing conventions.

Use modern, idiomatic Java. Prefer small cohesive classes, records for immutable carriers, and `var` where the inferred type is obvious. Avoid field injection, unnecessary inheritance, speculative abstractions, and comments that restate the code.

Do not perform unrelated refactors or change public APIs, database schemas, dependencies, or architectural boundaries unless the task requires it. Explain significant architectural choices before implementing them.

## Testing and Verification

Tests are part of the change:

1. Prefer unit tests for domain behavior.
2. Use focused Spring tests only when framework integration matters.
3. Use Testcontainers for real PostgreSQL behavior.
4. Add a regression test for bug fixes when practical.

Test observable behavior, not implementation details. Mock external boundaries rather than domain objects.

After changes:

- Compile and run relevant tests; run `./gradlew test` when practical.
- Report commands actually executed, failures, and skipped tests clearly.
- Do not claim unverified behavior works.
