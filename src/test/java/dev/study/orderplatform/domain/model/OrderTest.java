package dev.study.orderplatform.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void placesAPendingOrderAndCalculatesItsTotal() {
        Currency currency = Currency.getInstance("CAD");

        Order order = Order.place(
                UUID.fromString("01994d56-1200-7000-8000-000000000001"),
                "customer-123",
                currency,
                List.of(
                        new OrderLine("SKU-1", 2, new Money(new BigDecimal("10.2500"), currency)),
                        new OrderLine("SKU-2", 1, new Money(new BigDecimal("4.5000"), currency))),
                Instant.parse("2026-09-14T12:00:00Z"));

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.total()).isEqualTo(new Money(new BigDecimal("25.0000"), currency));
        assertThat(order.lines()).hasSize(2);
    }
}
