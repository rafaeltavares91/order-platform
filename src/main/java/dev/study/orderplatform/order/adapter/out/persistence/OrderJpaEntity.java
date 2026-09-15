package dev.study.orderplatform.order.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import dev.study.orderplatform.order.domain.model.Money;
import dev.study.orderplatform.order.domain.model.Order;
import dev.study.orderplatform.order.domain.model.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "customer_orders")
class OrderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = Money.PRECISION, scale = Money.SCALE)
    private BigDecimal total;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    @Column(nullable = false)
    private long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNumber ASC")
    private List<OrderLineJpaEntity> lines = new ArrayList<>();

    protected OrderJpaEntity() {
    }

    static OrderJpaEntity from(Order order) {
        var entity = new OrderJpaEntity();
        entity.id = order.id();
        entity.customerId = order.customerId();
        entity.status = order.status();
        entity.currency = order.total().currency().getCurrencyCode();
        entity.total = order.total().amount();
        entity.createdAt = order.createdAt();
        entity.version = order.version();

        for (int index = 0; index < order.lines().size(); index++) {
            entity.lines.add(OrderLineJpaEntity.from(entity, index + 1, order.lines().get(index)));
        }
        return entity;
    }

    Order toDomain() {
        Currency orderCurrency = Currency.getInstance(currency);
        return Order.rehydrate(
                id,
                customerId,
                status,
                lines.stream().map(line -> line.toDomain(orderCurrency)).toList(),
                new Money(total, orderCurrency),
                createdAt,
                version);
    }
}
