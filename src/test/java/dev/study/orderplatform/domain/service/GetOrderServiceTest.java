package dev.study.orderplatform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;

class GetOrderServiceTest {

    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");

    @Test
    void returnsTheOrderLoadedById() {
        Order order = Order.create(
                ORDER_ID,
                "customer-123",
                new Money(new BigDecimal("20.5000"), Currency.getInstance("CAD")),
                Instant.parse("2026-09-14T12:00:00Z"),
                LocalDate.parse("2026-09-15"));
        GetOrderService service = new GetOrderService(id -> Optional.of(order));

        assertThat(service.getById(ORDER_ID)).isSameAs(order);
    }

    @Test
    void reportsWhenTheOrderDoesNotExist() {
        GetOrderService service = new GetOrderService(id -> Optional.empty());

        assertThatThrownBy(() -> service.getById(ORDER_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order %s was not found", ORDER_ID);
    }
}
