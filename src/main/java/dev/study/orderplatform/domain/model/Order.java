package dev.study.orderplatform.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Order {

    private final UUID id;
    private final OrderStatus status;
    private final LocalDate creditDate;
    private final Money totalAmount;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String paymentId;
    private final Instant paidAt;

    private Order(
            UUID id,
            OrderStatus status,
            LocalDate creditDate,
            Money totalAmount,
            List<OrderItem> items,
            Instant createdAt,
            Instant updatedAt,
            String paymentId,
            Instant paidAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.creditDate = Objects.requireNonNull(creditDate, "creditDate must not be null");
        this.items = copyAndValidateItems(id, items);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt must not be before createdAt");
        }
        if (creditDate.isBefore(LocalDate.ofInstant(createdAt, ZoneOffset.UTC))) {
            throw new IllegalArgumentException("creditDate must not be before the creation date");
        }
        var calculatedTotal = calculateTotal(this.items);
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        if (!this.totalAmount.equals(calculatedTotal)) {
            throw new IllegalArgumentException("totalAmount must equal the sum of item amounts");
        }
        if ((paymentId == null) != (paidAt == null)) {
            throw new IllegalArgumentException("paymentId and paidAt must either both be set or both be null");
        }
        if (paymentId != null && paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId must not be blank");
        }
        this.paymentId = paymentId;
        this.paidAt = paidAt;
    }

    public static Order create(UUID id, LocalDate creditDate, List<OrderItem> items, Instant createdAt) {
        var totalAmount = calculateTotal(items);
        return new Order(
                id,
                OrderStatus.WAITING_PAYMENT,
                creditDate,
                totalAmount,
                items,
                createdAt,
                createdAt,
                null,
                null);
    }

    public static Order rehydrate(
            UUID id,
            OrderStatus status,
            LocalDate creditDate,
            Money totalAmount,
            List<OrderItem> items,
            Instant createdAt,
            Instant updatedAt,
            String paymentId,
            Instant paidAt) {
        return new Order(id, status, creditDate, totalAmount, items, createdAt, updatedAt, paymentId, paidAt);
    }

    public static Order rehydrate(
            UUID id,
            OrderStatus status,
            LocalDate creditDate,
            Money totalAmount,
            List<OrderItem> items,
            Instant createdAt,
            Instant updatedAt) {
        return rehydrate(id, status, creditDate, totalAmount, items, createdAt, updatedAt, null, null);
    }

    public Order credit(String paymentId, Instant paidAt, Instant updatedAt) {
        if (status != OrderStatus.WAITING_PAYMENT) {
            throw new IllegalStateException("only orders waiting for payment can be credited");
        }
        var creditedItems = items.stream().map(item -> item.credit(updatedAt)).toList();
        return new Order(
                id,
                OrderStatus.CREDITED,
                creditDate,
                totalAmount,
                creditedItems,
                createdAt,
                updatedAt,
                Objects.requireNonNull(paymentId, "paymentId must not be null"),
                Objects.requireNonNull(paidAt, "paidAt must not be null"));
    }

    private static List<OrderItem> copyAndValidateItems(UUID orderId, List<OrderItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        var copy = List.copyOf(items);
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }

        var itemIds = new HashSet<UUID>();
        for (var item : copy) {
            if (!orderId.equals(item.orderId())) {
                throw new IllegalArgumentException("every item must belong to the order");
            }
            if (!itemIds.add(item.id())) {
                throw new IllegalArgumentException("item ids must be unique within an order");
            }
        }
        return copy;
    }

    private static Money calculateTotal(List<OrderItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }

        var total = Money.zero(items.getFirst().amount().currency());
        for (var item : items) {
            total = total.add(item.amount());
        }
        return total;
    }

    public UUID id() {
        return id;
    }

    public OrderStatus status() {
        return status;
    }

    public LocalDate creditDate() {
        return creditDate;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public List<OrderItem> items() {
        return items;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public String paymentId() {
        return paymentId;
    }

    public Instant paidAt() {
        return paidAt;
    }

}
