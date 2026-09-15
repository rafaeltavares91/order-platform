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
    "customerId": "customer-123",
    "currency": "CAD",
    "lines": [
      {"sku": "SKU-1", "quantity": 2, "unitPrice": 10.2500}
    ]
  }'
```

Retrieve an order:

```shell
curl http://localhost:8080/orders/{orderId}
```

Health endpoints are available at `/actuator/health`, `/actuator/health/liveness`, and `/actuator/health/readiness`.

## Architecture

The project is a single deployable Gradle module organized by business capability. Inside the `order` capability:

- `domain` contains framework-free business types and invariants.
- `application` contains use cases and inbound/outbound ports.
- `adapter.in.web` translates HTTP requests and responses.
- `adapter.out.persistence` translates between the domain and JPA.

Dependencies point inward. ArchUnit tests protect the most important boundaries without requiring separate build modules.

Flyway exclusively owns the schema. Hibernate validates it and never creates or updates it. Production deployments should run Flyway with a dedicated migration identity before rolling out application instances; the runtime database identity should have only data-access privileges.

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
- Time: `Instant` in Java and `timestamptz` in PostgreSQL; time-dependent behavior receives a `Clock`
- PostgreSQL 18.6 for local development and tests
- Optimistic locking through a `version` column
- Spring MVC and JPA; reactive infrastructure is intentionally absent
- Kafka, outbox, sagas, CQRS, Redis, exporters, and resilience libraries are intentionally deferred
