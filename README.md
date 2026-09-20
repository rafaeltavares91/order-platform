# Order Platform

A production-oriented order processing service built with Java 25, Spring Boot, PostgreSQL, and a Pragmatic Layered Architecture with Domain Separation.

## Requirements

- Java 25 (Gradle can provision a matching toolchain automatically)
- Docker with Docker Compose for the local database and integration tests

## Run locally

Create a free LocalStack Hobby account, expose its token, and start the local dependencies:

```shell
export LOCALSTACK_AUTH_TOKEN=your-token
docker compose up -d postgres localstack keycloak
```

Run the application:

```shell
./gradlew bootRun --args='--spring.profiles.active=local'
```

The `local` and `dev` profiles create three sample customers on startup. Their fixed IDs are
`01994d56-1200-7000-8000-000000000004`, `01994d56-1200-7000-8000-000000000005`, and
`01994d56-1200-7000-8000-000000000006`. Existing sample customers are preserved, so restarting
the application does not duplicate them.

The LocalStack initializer creates the SNS topics, SQS queues, subscriptions, filters, queue policies, and dead-letter queues. Keycloak imports a development realm with machine-to-machine clients. All credentials in `compose.yaml` and the realm file are development-only.

Obtain a read/write access token:

```shell
ACCESS_TOKEN=$(curl --silent --request POST \
  http://localhost:8081/realms/order-platform/protocol/openid-connect/token \
  --user order-platform-read-write:local-read-write-secret \
  --data grant_type=client_credentials | jq --raw-output .access_token)
```

## API

Create an order:

```shell
curl --request POST http://localhost:8080/orders \
  --header 'Content-Type: application/json' \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --data '{
    "creditDate": "2099-09-15",
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
curl --header "Authorization: Bearer $ACCESS_TOKEN" http://localhost:8080/orders/{orderId}
```

New orders are persisted as `WAITING_PAYMENT`. The same transaction stores a `PaymentRequested.v1` outbox event, which is retried until it is published to the `order-events` SNS topic. LocalStack routes it to the `order-payment-requested` SQS queue through a filtered subscription.

Publish a local payment confirmation through the `payment-events` SNS topic:

```shell
./localstack/publish-payment-confirmed.sh {orderId} PAY-001 25.0000 CAD
```

The `order-payment-confirmed` consumer validates the amount and currency and atomically credits all customers and changes the order and its items to `CREDITED`. Message and payment identifiers make retries idempotent. Invalid messages are retried and eventually moved to `order-payment-confirmed-dlq`.

The API is an OAuth2 Resource Server. `POST /orders` requires `orders:write`, `GET /orders/{id}` requires `orders:read`, and health endpoints remain public. Authentication uses Keycloak locally; a future Cognito user pool can issue equivalent client-credentials tokens by changing the configured issuer, JWK set, and audience.

Health endpoints are available at `/actuator/health`, `/actuator/health/liveness`, and `/actuator/health/readiness`.

## Architecture

The project follows a **Pragmatic Layered Architecture with Domain Separation**. It is a single deployable Gradle module organized into clear concerns:

- `domain.model` contains entities, value objects, and business invariants.
- `domain.service` coordinates application and use-case behavior.
- `domain.port` contains existing contracts for external capabilities where abstraction provides concrete isolation or testability value.
- `web.controller`, `web.dto`, and `web.error` expose the HTTP API.
- `persistence.entity`, `persistence.repository`, and `persistence.adapter` implement explicit infrastructure behavior, including JPA persistence and UUID generation.
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

The project uses a pragmatic testing strategy aligned with its layered architecture:

- Domain and service behavior is covered by focused, fast unit tests without Spring.
- Controller and API behavior should be covered by application integration tests exercising the complete HTTP-to-PostgreSQL flow without mocked services or repositories.
- Testcontainers provides real PostgreSQL instances and Flyway initializes integration-test schemas.
- Dedicated persistence integration tests are reserved for custom queries, important constraints, non-trivial mappings, locking, concurrency, or transaction behavior.
- Tests target meaningful observable behavior rather than framework internals or a numeric coverage percentage.

Database-backed tests are skipped when Docker is unavailable; CI should provide Docker so integration tests always execute.

## Explicit initial decisions

- Base package: `dev.study.orderplatform`
- Unversioned resource-oriented URLs
- Application-generated UUIDv7 identifiers
- Money: `BigDecimal`, ISO 4217 currency, `numeric(19,4)`, and no implicit rounding
- Time: `Instant`/`timestamptz` for creation events and `LocalDate`/`date` for scheduled business dates; time-dependent behavior receives a `Clock`
- PostgreSQL 18.6 for local development and tests
- Internal `BIGINT` primary keys generated by database sequences and unique UUIDv7 public identifiers
- Orders contain one or more customer items and derive their persisted total from those items
- Separate `orders`, `order_items`, and `customers` tables with internal `BIGINT` foreign keys and without implicit JPA relationships
- Spring MVC and JPA; reactive infrastructure is intentionally absent
- SNS topics fan out events to dedicated SQS queues; delivery is at least once and consumers are idempotent
- A transactional outbox protects order creation from broker availability and an inbox deduplicates consumed events
- Kafka, sagas, CQRS, Redis, exporters, and resilience libraries remain deferred
