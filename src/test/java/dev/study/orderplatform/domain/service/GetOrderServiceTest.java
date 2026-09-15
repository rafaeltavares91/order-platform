package dev.study.orderplatform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import dev.study.orderplatform.domain.exception.OrderNotFoundException;
import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderLine;

class GetOrderServiceTest {

    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");

    @Test
    void returnsTheOrderLoadedById() {
        Currency currency = Currency.getInstance("CAD");
        Order order = Order.place(
                ORDER_ID,
                "customer-123",
                currency,
                List.of(new OrderLine("SKU-1", 2, new Money(new BigDecimal("10.2500"), currency))),
                Instant.parse("2026-09-14T12:00:00Z"));
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
