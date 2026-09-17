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
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID id;

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

    @Version
    @Column(nullable = false)
    private long version;

    protected OrderEntity() {
    }

    public static OrderEntity from(Order order) {
        var entity = new OrderEntity();
        entity.id = order.id();
        entity.status = order.status();
        entity.creditDate = order.creditDate();
        entity.totalAmount = order.totalAmount().amount();
        entity.currency = order.totalAmount().currency().getCurrencyCode();
        entity.createdAt = order.createdAt();
        entity.updatedAt = order.updatedAt();
        entity.version = order.version();
        return entity;
    }

    public Order toDomain(List<OrderItem> items) {
        return Order.rehydrate(
                id,
                status,
                creditDate,
                new Money(totalAmount, Currency.getInstance(currency)),
                items,
                createdAt,
                updatedAt,
                version);
    }
}
