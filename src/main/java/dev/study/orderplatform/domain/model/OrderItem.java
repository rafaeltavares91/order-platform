package dev.study.orderplatform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class OrderItem {

    private final UUID id;
    private final UUID orderId;
    private final UUID customerId;
    private final Money amount;
    private final OrderItemStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private OrderItem(
            UUID id,
            UUID orderId,
            UUID customerId,
            Money amount,
            OrderItemStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.orderId = Objects.requireNonNull(orderId, "orderId must not be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt must not be before createdAt");
        }
    }

    public static OrderItem create(UUID id, UUID orderId, UUID customerId, Money amount, Instant createdAt) {
        return new OrderItem(id, orderId, customerId, amount, OrderItemStatus.PENDING, createdAt, createdAt);
    }

    public static OrderItem rehydrate(
            UUID id,
            UUID orderId,
            UUID customerId,
            Money amount,
            OrderItemStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new OrderItem(id, orderId, customerId, amount, status, createdAt, updatedAt);
    }

    public UUID id() {
        return id;
    }

    public UUID orderId() {
        return orderId;
    }

    public UUID customerId() {
        return customerId;
    }

    public Money amount() {
        return amount;
    }

    public OrderItemStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

}
