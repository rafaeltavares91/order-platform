package dev.study.orderplatform.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.OrderItem;
import dev.study.orderplatform.domain.model.OrderItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "item_index", nullable = false)
    private int itemIndex;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, precision = Money.PRECISION, scale = Money.SCALE)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderItemStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected OrderItemEntity() {
    }

    public static OrderItemEntity from(OrderItem item, int itemIndex) {
        var entity = new OrderItemEntity();
        entity.id = item.id();
        entity.orderId = item.orderId();
        entity.itemIndex = itemIndex;
        entity.customerId = item.customerId();
        entity.amount = item.amount().amount();
        entity.currency = item.amount().currency().getCurrencyCode();
        entity.status = item.status();
        entity.createdAt = item.createdAt();
        entity.updatedAt = item.updatedAt();
        entity.version = item.version();
        return entity;
    }

    public OrderItem toDomain() {
        return OrderItem.rehydrate(
                id,
                orderId,
                customerId,
                new Money(amount, Currency.getInstance(currency)),
                status,
                createdAt,
                updatedAt,
                version);
    }
}
