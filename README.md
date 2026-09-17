# Order Platform

A production-oriented order processing service built with Java 25, Spring Boot, PostgreSQL, and a pragmatic hexagonal architecture.

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

The project is a single deployable Gradle module with a compact package structure:

- `application` contains framework-free use-case orchestration and commands.
- `domain.model` and `domain.port` contain business rules and infrastructure-independent output contracts.
- `web.controller`, `web.dto`, and `web.error` expose the HTTP API.
- `persistence.adapter`, `persistence.repository`, and `persistence.entity` implement persistence with Spring Data and JPA.
- `identifier` provides order identifiers.
- `configuration` wires the domain to its infrastructure.

Dependencies follow `web -> application -> domain <- persistence`. ArchUnit tests protect the most important boundaries without requiring separate build modules.

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

Domain and application tests run without Spring. PostgreSQL integration tests use Testcontainers and are skipped when Docker is unavailable; CI should provide Docker so those tests always execute.

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
