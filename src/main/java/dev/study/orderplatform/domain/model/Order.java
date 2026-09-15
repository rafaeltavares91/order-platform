package dev.study.orderplatform.domain.model;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Order {

    private final UUID id;
    private final String customerId;
    private final OrderStatus status;
    private final List<OrderLine> lines;
    private final Money total;
    private final Instant createdAt;
    private final long version;

    private Order(
            UUID id,
            String customerId,
            OrderStatus status,
            List<OrderLine> lines,
            Money total,
            Instant createdAt,
            long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (customerId.length() > 100) {
            throw new IllegalArgumentException("customerId must not exceed 100 characters");
        }
        this.customerId = customerId;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.lines = List.copyOf(lines);
        if (this.lines.isEmpty()) {
            throw new IllegalArgumentException("an order must contain at least one line");
        }
        this.total = Objects.requireNonNull(total, "total must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
        this.version = version;
    }

    public static Order place(UUID id, String customerId, Currency currency, List<OrderLine> lines, Instant createdAt) {
        Objects.requireNonNull(currency, "currency must not be null");
        List<OrderLine> copiedLines = List.copyOf(lines);
        Money total = copiedLines.stream()
                .map(OrderLine::subtotal)
                .reduce(Money.zero(currency), Money::add);
        return new Order(id, customerId, OrderStatus.PENDING, copiedLines, total, createdAt, 0);
    }

    public static Order rehydrate(
            UUID id,
            String customerId,
            OrderStatus status,
            List<OrderLine> lines,
            Money total,
            Instant createdAt,
            long version) {
        return new Order(id, customerId, status, lines, total, createdAt, version);
    }

    public UUID id() {
        return id;
    }

    public String customerId() {
        return customerId;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    public Money total() {
        return total;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
