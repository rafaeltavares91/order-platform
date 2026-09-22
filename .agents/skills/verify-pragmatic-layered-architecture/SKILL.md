---
name: verify-pragmatic-layered-architecture
description: Audit Java and Spring changes in Order Platform for compliance with its Pragmatic Layered Architecture with Decoupled Domain. Use for architecture reviews, boundary checks, dependency analysis, or when assessing whether a class belongs in domain, web, persistence, messaging, or configuration; do not use as a generic style or correctness review.
---

# Verify Pragmatic Layered Architecture

Treat the repository's nearest `AGENTS.md`, architecture documentation, and current package structure as authoritative; report inconsistencies between them rather than silently choosing one.

## Scope the audit

Determine whether the user wants one file, the current diff, a package, or the entire repository reviewed. If no scope is stated, inspect the current diff when it exists; otherwise inspect the named files and their direct collaborators. For each reviewed class, read enough callers, callees, interfaces, mappings, tests, and configuration to judge the boundary in context.

For this project, start with:

- base package `dev.study.orderplatform`;
- `domain.model` for entities, value objects, domain events, and invariants;
- `domain.service` for use-case coordination;
- `domain.port` for valuable external-boundary contracts;
- `web` for controllers, HTTP DTOs, validation, and error translation;
- `persistence` for JPA entities, Spring Data repositories, mapping, transactions, database operations, and infrastructure adapters;
- `messaging` for broker-facing listeners, publishers, and message representations;
- `configuration` for Spring wiring.

Do not presume every class must have a port, every service must have an interface, or controllers must call a separate application layer. Those would contradict the chosen architecture unless a concrete need justifies them.

## Gather evidence

Read the governing files before deciding: `AGENTS.md`, the Architecture section of `README.md`, the relevant production classes, and `src/test/java/dev/study/orderplatform/architecture/ArchitectureTest.java`. Use `rg` to inspect imports and cross-package references. Prefer semantic inspection over package-name matching alone.

When execution is appropriate, run the smallest useful existing architecture test first:

```shell
./gradlew test --tests dev.study.orderplatform.architecture.ArchitectureTest
```

Broaden to relevant unit or integration tests only when the reviewed boundary or behavior needs it. Never claim a command passed if it was not run. A passing ArchUnit test is evidence for its encoded rules, not proof that all architectural responsibilities are correct.

## Enforce hard boundaries

Treat these as violations unless an explicit project rule supersedes them:

1. `domain` imports Spring, Jakarta Persistence, web DTOs, persistence entities or repositories, messaging representations, or configuration classes.
2. Web code imports persistence implementation types or accesses repositories directly.
3. Infrastructure representations leak into domain APIs, including JPA entities or HTTP/message DTOs used as domain inputs or results.
4. A single representation is reused as domain model, HTTP DTO, and persistence entity. Require explicit boundary mapping; mapping methods may live on infrastructure-side representations when they do not push infrastructure concerns into the domain.
5. Business invariants that determine valid domain state are implemented only in controllers, listeners, adapters, or JPA entities instead of domain objects.
6. Spring creates schema state outside Flyway, or a schema change lacks a Flyway migration. Hibernate must validate rather than create or update the schema.
7. Time-dependent domain or service behavior reads the system clock directly instead of receiving `Clock`, or persistence identifiers that must be UUIDv7 are delegated to the database or random UUID generation.

Also verify dependency direction:

- domain models depend only on the JDK and other domain types;
- domain services may depend on domain models, domain exceptions, domain ports, and JDK types;
- web and messaging may invoke domain services and translate external representations;
- persistence may implement domain ports and map between domain and storage representations;
- configuration may depend on all layers solely to assemble runtime components.

## Apply pragmatic judgment

Report these as design warnings only when concrete evidence shows harm:

- an adapter has multiple repositories or performs mapping and transactional persistence;
- a controller calls a concrete domain service directly;
- a JPA entity exposes `from` or `toDomain` mapping methods;
- a service lacks an interface;
- related external operations share one port or adapter;
- messaging or persistence coordinates atomic infrastructure work such as outbox or inbox handling.

These shapes are permitted here. Escalate them only for a demonstrated problem such as mixed business policy, reversed dependencies, poor test isolation, non-atomic state changes, representation leakage, or unrelated responsibilities that change for different reasons.

Evaluate ports by value, not symmetry. A port is justified for an external capability when it isolates the domain service from infrastructure or materially improves testing. Flag speculative, pass-through, or one-interface-per-class indirection; do not recommend removing an existing port merely because a direct dependency is technically possible.

For classes such as `OrderPersistenceAdapter`, distinguish infrastructure orchestration from business policy. Repository calls, explicit domain/entity conversion, transaction boundaries, public-to-internal identifier resolution, and transactional outbox writes belong in persistence. Rules that decide whether an order is valid, how totals are calculated, or which state transitions are allowed belong in the domain.

## Review quality attributes tied to boundaries

Check only when relevant to the reviewed change:

- money remains `BigDecimal` plus ISO 4217 currency at scale 4 without implicit rounding;
- creation timestamps use `Instant`, business dates use `LocalDate`, and time is injected;
- transaction boundaries cover state that must change atomically;
- network and broker failures do not break database consistency assumptions;
- tests match the layer: plain deterministic service/domain unit tests, end-to-end HTTP-to-PostgreSQL API integration tests, and focused persistence integration tests only for non-trivial database behavior.

Do not turn these checks into broad product, security, performance, or code-style reviews unless they directly demonstrate an architectural boundary problem.

## Report findings

Lead with findings ordered by severity. For every violation include:

- the file and precise line;
- the observed dependency or responsibility;
- the violated project rule;
- the concrete consequence;
- the smallest coherent correction.

Separate definite violations from design warnings and optional improvements. Do not invent issues to fill categories. If no violation is found, say so explicitly, state the inspected scope and commands run, and mention residual gaps such as unexecuted tests or architecture rules that are not encoded in ArchUnit.

Do not modify code during an audit unless the user also asks for a fix. If asked to enforce a stable mechanical boundary, extend `ArchitectureTest` with the smallest rule that captures the invariant without banning allowed pragmatic dependencies. Use tests for dependency direction; keep responsibility and cohesion judgments in review rather than encoding brittle naming conventions.
