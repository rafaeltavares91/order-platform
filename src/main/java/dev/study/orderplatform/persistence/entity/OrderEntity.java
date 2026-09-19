package dev.study.orderplatform.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItem;
import dev.study.orderplatform.domain.model.OrderStatus;
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
        name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "uq_orders_public_id", columnNames = "public_id"))
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_id_generator")
    @SequenceGenerator(name = "order_id_generator", sequenceName = "orders_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "credit_date", nullable = false)
    private LocalDate creditDate;

    @Column(name = "total_amount", nullable = false, precision = Money.PRECISION, scale = Money.SCALE)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderEntity() {
    }

    public static OrderEntity from(Order order) {
        var entity = new OrderEntity();
        entity.publicId = order.id();
        entity.status = order.status();
        entity.creditDate = order.creditDate();
        entity.totalAmount = order.totalAmount().amount();
        entity.currency = order.totalAmount().currency().getCurrencyCode();
        entity.createdAt = order.createdAt();
        entity.updatedAt = order.updatedAt();
        return entity;
    }

    public Order toDomain(List<OrderItem> items) {
        return Order.rehydrate(
                publicId,
                status,
                creditDate,
                new Money(totalAmount, Currency.getInstance(currency)),
                items,
                createdAt,
                updatedAt);
    }

    public Long internalId() {
        return id;
    }

    public UUID publicId() {
        return publicId;
    }
}
