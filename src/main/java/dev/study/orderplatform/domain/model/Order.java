package dev.study.orderplatform.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

public final class Order {

    private final UUID id;
    private final String customerId;
    private final Money amount;
    private final OrderStatus status;
    private final Instant createdAt;
    private final LocalDate creditDate;
    private final long version;

    private Order(
            UUID id,
            String customerId,
            Money amount,
            OrderStatus status,
            Instant createdAt,
            LocalDate creditDate,
            long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (customerId.length() > 100) {
            throw new IllegalArgumentException("customerId must not exceed 100 characters");
        }
        this.customerId = customerId;
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.creditDate = Objects.requireNonNull(creditDate, "creditDate must not be null");
        if (creditDate.isBefore(LocalDate.ofInstant(createdAt, ZoneOffset.UTC))) {
            throw new IllegalArgumentException("creditDate must not be before the creation date");
        }
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
        this.version = version;
    }

    public static Order create(
            UUID id, String customerId, Money amount, Instant createdAt, LocalDate creditDate) {
        return new Order(id, customerId, amount, OrderStatus.CREATED, createdAt, creditDate, 0);
    }

    public static Order rehydrate(
            UUID id,
            String customerId,
            Money amount,
            OrderStatus status,
            Instant createdAt,
            LocalDate creditDate,
            long version) {
        return new Order(id, customerId, amount, status, createdAt, creditDate, version);
    }

    public UUID id() {
        return id;
    }

    public String customerId() {
        return customerId;
    }

    public Money amount() {
        return amount;
    }

    public OrderStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public LocalDate creditDate() {
        return creditDate;
    }

    public long version() {
        return version;
    }
}
