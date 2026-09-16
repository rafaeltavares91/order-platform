package dev.study.orderplatform.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "customer_orders")
public class OrderEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(nullable = false, precision = Money.PRECISION, scale = Money.SCALE)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "credit_date", nullable = false)
    private LocalDate creditDate;

    @Version
    @Column(nullable = false)
    private long version;

    protected OrderEntity() {
    }

    public static OrderEntity from(Order order) {
        var entity = new OrderEntity();
        entity.id = order.id();
        entity.customerId = order.customerId();
        entity.amount = order.amount().amount();
        entity.currency = order.amount().currency().getCurrencyCode();
        entity.status = order.status();
        entity.createdAt = order.createdAt();
        entity.creditDate = order.creditDate();
        entity.version = order.version();
        return entity;
    }

    public Order toDomain() {
        return Order.rehydrate(
                id,
                customerId,
                new Money(amount, Currency.getInstance(currency)),
                status,
                createdAt,
                creditDate,
                version);
    }
}
