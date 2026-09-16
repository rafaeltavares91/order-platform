# AGENTS.md

## Project

Order Platform is a production-oriented order processing API built as a learning project focused on modern Java backend engineering, software architecture, reliability, and distributed systems.

The codebase should favor production-quality engineering practices while remaining pragmatic and understandable. Complexity must be introduced only when it solves a concrete problem.

## Technology

Current core stack:

* Java 25
* Spring Boot
* Gradle
* PostgreSQL
* Flyway
* JUnit 5
* AssertJ
* Mockito
* Testcontainers
* Docker Compose

Technologies such as Kafka, Redis, OpenTelemetry, Prometheus, and Grafana may be introduced later when required by the system design.

Do not introduce new frameworks, libraries, infrastructure components, or architectural patterns without explaining why they are needed.

## Architecture

The project uses a lightweight architecture inspired by Hexagonal Architecture and Domain-Driven Design.

Current high-level packages:

* `domain`

    * `model`
    * `service`
    * `port`
    * `exception`
* `web`

    * `controller`
    * `dto`
    * `error`
* `persistence`

    * `entity`
    * `repository`

The architecture is intentionally pragmatic rather than strictly layered.

### Dependency rules

The domain is the center of the application.

Domain code must not depend on:

* Spring
* JPA/Hibernate
* web/API DTOs
* persistence entities
* PostgreSQL-specific concepts
* messaging infrastructure
* other infrastructure concerns

Domain services may depend on abstractions defined as domain ports.

Infrastructure implements those ports.

Controllers may depend directly on domain services. Do not introduce interfaces between controllers and services merely for architectural symmetry.

Prefer dependency inversion at meaningful external boundaries rather than abstraction between every class.

## Domain Modeling

Keep business rules in the domain whenever possible.

Prefer behavior-rich domain objects over anemic models.

Domain entities and value objects should protect their own invariants.

Prefer:

`order.cancel()`

over moving domain behavior into procedural service code.

Services should primarily coordinate a use case when the behavior does not naturally belong to one domain object.

Use value objects when a concept has meaningful invariants or behavior, but avoid creating value objects merely to wrap every primitive.

## Code Style

Use modern, idiomatic Java.

Prefer:

* immutable objects where practical
* constructor injection
* small cohesive classes
* meaningful names
* explicit domain concepts
* records for appropriate immutable data carriers
* `var` when the inferred type is obvious and improves readability
* standard library solutions when sufficient

Avoid:

* field injection
* unnecessary inheritance
* speculative abstractions
* excessive comments explaining obvious code

Code should communicate intent primarily through structure and naming.

## Spring

Keep Spring at the edges whenever practical.

Do not add Spring annotations to domain models.

Configuration should be explicit when it improves understanding or testability.

Do not use framework features merely to reduce a few lines of straightforward code if doing so hides important behavior.

## Persistence

Domain models and persistence entities are separate concepts.

JPA entities belong in the persistence layer.

Repositories/adapters must translate between persistence representations and domain models.

Database schema changes must be managed through Flyway migrations.

Do not rely on Hibernate automatic schema generation as the production schema-management strategy.

Avoid leaking persistence-specific behavior into the domain.

## Testing

Tests are part of the implementation, not an optional follow-up.

Use the smallest appropriate test scope.

Prefer:

1. Unit tests for domain behavior and business rules.
2. Focused tests for individual components when framework integration matters.
3. Integration tests with Testcontainers when real infrastructure behavior matters.
4. End-to-end tests only for important application flows.

Do not mock domain objects simply to isolate code.

Mock external boundaries when appropriate.

Prefer testing observable behavior rather than implementation details.

A refactoring that preserves behavior should generally not require rewriting large portions of the tests.

Bug fixes should include a regression test when practical.

## Reliability

The system will progressively explore distributed-systems concerns such as:

* idempotency
* transactional outbox
* asynchronous messaging
* Kafka
* retries and backoff
* duplicate message handling
* eventual consistency
* Saga
* CQRS
* observability

Do not introduce these patterns prematurely.

When implementing them, correctness and failure scenarios are more important than demonstrating the pattern itself.

Always consider failure modes when working with database transactions, network calls, or messaging.

## Working With This Repository

Before making a meaningful change:

1. Inspect the relevant existing code.
2. Understand the current architecture and conventions.
3. Prefer extending existing patterns over introducing new ones.
4. Identify the smallest coherent change that solves the problem.

Do not perform broad refactors unrelated to the requested task.

Do not change public APIs, database schemas, dependencies, architectural boundaries, or established conventions unless required by the task.

If a request would require a significant architectural decision, explain the options and trade-offs before implementing it.

## AI-Assisted Development

This repository is also used to study AI-assisted software engineering.

Do not optimize for producing the maximum amount of code.

Optimize for:

* correctness
* maintainability
* clarity
* testability
* explaining meaningful engineering decisions
* keeping changes reviewable

When there are multiple reasonable approaches, prefer discussing the trade-offs rather than silently choosing the most complex solution.

Do not hide complexity behind generated code.

Do not introduce code the user would have difficulty reasoning about without explaining the underlying design.

## Verification

After making changes:

* compile the project
* run relevant tests
* run the full test suite when practical
* report failures clearly
* distinguish failures caused by the change from pre-existing failures

Do not claim a command, test, migration, or application flow works unless it was actually executed or otherwise verified.

## Guiding Principle

Build the simplest design that correctly solves the current problem while preserving clear boundaries for likely future evolution.
