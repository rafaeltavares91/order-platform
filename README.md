# Order Platform

A production-oriented order processing service built with Java 25, Spring Boot, PostgreSQL, and a Pragmatic Layered Architecture with Domain Separation.

## Requirements

- Java 25 (Gradle can provision a matching toolchain automatically)
- Docker with Docker Compose for the local database and integration tests

## Run locally

Start PostgreSQL:

```shell
docker compose up -d postgres
```

Run the application:

```shell
./gradlew bootRun
```

The default local credentials in `compose.yaml` are development-only. Override `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in deployed environments.

## API

Create an order:

```shell
curl --request POST http://localhost:8080/orders \
  --header 'Content-Type: application/json' \
  --data '{
    "creditDate": "2026-09-15",
    "items": [
      {
        "customerId": "01994d56-1200-7000-8000-000000000004",
        "amount": 20.5000,
        "currency": "CAD"
      },
      {
        "customerId": "01994d56-1200-7000-8000-000000000005",
        "amount": 4.5000,
        "currency": "CAD"
      }
    ]
  }'
```

Customers referenced by `customerId` must already exist. The application calculates `totalAmount` from the items; clients cannot supply it. All items in an order must use the same currency.

Retrieve an order:

```shell
curl http://localhost:8080/orders/{orderId}
```

Health endpoints are available at `/actuator/health`, `/actuator/health/liveness`, and `/actuator/health/readiness`.

## Architecture

The project follows a **Pragmatic Layered Architecture with Domain Separation**. It is a single deployable Gradle module organized into clear concerns:

- `domain.model` contains entities, value objects, and business invariants.
- `domain.service` coordinates application and use-case behavior.
- `domain.port` contains existing contracts for external capabilities where abstraction provides concrete isolation or testability value.
- `web.controller`, `web.dto`, and `web.error` expose the HTTP API.
- `persistence.entity`, `persistence.repository`, and `persistence.adapter` implement explicit persistence behavior with JPA and Spring Data.
- `identifier` provides application-generated identifiers.
- `configuration` provides Spring wiring.

Domain models remain independent from HTTP DTOs, JPA entities, and framework-specific representations. Web DTOs, persistence entities, and domain models are mapped explicitly and are not reused across boundaries merely for convenience. Services may depend on interfaces for meaningful external boundaries, while controllers may call services directly.

This is not intended to be a strict implementation of Hexagonal Architecture, Ports and Adapters, Clean Architecture, or another named pattern. New ports, adapters, interactors, application layers, repository interfaces, or similar indirection should be introduced only when they solve a concrete problem. Existing abstractions remain where they currently provide useful isolation or testability. ArchUnit tests protect the important domain and layer boundaries without requiring separate build modules.

Flyway exclusively owns the schema. Hibernate validates it and never creates or updates it. Production deployments should run Flyway with a dedicated migration identity before rolling out application instances; the runtime database identity should have only data-access privileges.

The current development schema is intentionally defined as a clean baseline migration. If a local volume was created with an older schema, recreate it before starting the application:

```shell
docker compose down --volumes
docker compose up -d postgres
```

## Tests

```shell
./gradlew test
```

Domain and service tests run without Spring. PostgreSQL integration tests use Testcontainers and are skipped when Docker is unavailable; CI should provide Docker so those tests always execute.

## Explicit initial decisions

- Base package: `dev.study.orderplatform`
- Unversioned resource-oriented URLs
- Application-generated UUIDv7 identifiers
- Money: `BigDecimal`, ISO 4217 currency, `numeric(19,4)`, and no implicit rounding
- Time: `Instant`/`timestamptz` for creation events and `LocalDate`/`date` for scheduled business dates; time-dependent behavior receives a `Clock`
- PostgreSQL 18.6 for local development and tests
- Optimistic locking through a `version` column
- Orders contain one or more customer items and derive their persisted total from those items
- Separate `orders`, `order_items`, and `customers` tables without implicit JPA relationships
- Spring MVC and JPA; reactive infrastructure is intentionally absent
- Kafka, outbox, sagas, CQRS, Redis, exporters, and resilience libraries are intentionally deferred
