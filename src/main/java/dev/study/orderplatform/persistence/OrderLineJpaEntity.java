package dev.study.orderplatform.persistence;

import java.math.BigDecimal;
import java.util.Currency;

import dev.study.orderplatform.domain.Money;
import dev.study.orderplatform.domain.OrderLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_lines")
class OrderLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private OrderJpaEntity order;

    @Column(name = "line_number", nullable = false, updatable = false)
    private int lineNumber;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = Money.PRECISION, scale = Money.SCALE)
    private BigDecimal unitPrice;

    protected OrderLineJpaEntity() {
    }

    static OrderLineJpaEntity from(OrderJpaEntity order, int lineNumber, OrderLine line) {
        var entity = new OrderLineJpaEntity();
        entity.order = order;
        entity.lineNumber = lineNumber;
        entity.sku = line.sku();
        entity.quantity = line.quantity();
        entity.unitPrice = line.unitPrice().amount();
        return entity;
    }

    OrderLine toDomain(Currency currency) {
        return new OrderLine(sku, quantity, new Money(unitPrice, currency));
    }
}
