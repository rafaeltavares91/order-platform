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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "order_items",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_order_items_public_id", columnNames = "public_id"),
            @UniqueConstraint(name = "uq_order_items_order_index", columnNames = {"order_id", "item_index"})
        })
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_id_generator")
    @SequenceGenerator(name = "order_item_id_generator", sequenceName = "order_items_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "item_index", nullable = false)
    private int itemIndex;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

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

    protected OrderItemEntity() {
    }

    public static OrderItemEntity from(OrderItem item, Long orderId, Long customerId, int itemIndex) {
        var entity = new OrderItemEntity();
        entity.publicId = item.id();
        entity.orderId = orderId;
        entity.itemIndex = itemIndex;
        entity.customerId = customerId;
        entity.amount = item.amount().amount();
        entity.currency = item.amount().currency().getCurrencyCode();
        entity.status = item.status();
        entity.createdAt = item.createdAt();
        entity.updatedAt = item.updatedAt();
        return entity;
    }

    public OrderItem toDomain(UUID orderPublicId, UUID customerPublicId) {
        return OrderItem.rehydrate(
                publicId,
                orderPublicId,
                customerPublicId,
                new Money(amount, Currency.getInstance(currency)),
                status,
                createdAt,
                updatedAt);
    }

    public Long customerInternalId() {
        return customerId;
    }
}
