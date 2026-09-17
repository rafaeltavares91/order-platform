package dev.study.orderplatform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.port.IdentifierGenerator;
import dev.study.orderplatform.domain.port.SaveOrderPort;

class CreateOrderServiceTest {

    @Test
    void createsAnOrderAndItsItemsUsingInjectedIdentityAndTime() {
        var orderId = UUID.fromString("01994d56-1200-7000-8000-000000000001");
        var firstItemId = UUID.fromString("01994d56-1200-7000-8000-000000000002");
        var secondItemId = UUID.fromString("01994d56-1200-7000-8000-000000000003");
        var firstCustomerId = UUID.fromString("01994d56-1200-7000-8000-000000000004");
        var secondCustomerId = UUID.fromString("01994d56-1200-7000-8000-000000000005");
        var now = Instant.parse("2026-09-14T12:00:00Z");
        var identifiers = new ArrayDeque<>(List.of(orderId, firstItemId, secondItemId));
        IdentifierGenerator identifierGenerator = identifiers::removeFirst;
        var savedOrder = new AtomicReference<Order>();
        SaveOrderPort saveOrder = order -> {
            savedOrder.set(order);
            return order;
        };
        var service = new CreateOrderService(
                saveOrder, identifierGenerator, Clock.fixed(now, ZoneOffset.UTC));
        var command = new CreateOrderCommand(
                LocalDate.parse("2026-09-15"),
                List.of(
                        new CreateOrderCommand.Item(firstCustomerId, money("20.5000")),
                        new CreateOrderCommand.Item(secondCustomerId, money("4.5000"))));

        var created = service.create(command);

        assertThat(created).isSameAs(savedOrder.get());
        assertThat(created.id()).isEqualTo(orderId);
        assertThat(created.createdAt()).isEqualTo(now);
        assertThat(created.updatedAt()).isEqualTo(now);
        assertThat(created.totalAmount()).isEqualTo(money("25.0000"));
        assertThat(created.items()).extracting(item -> item.id()).containsExactly(firstItemId, secondItemId);
        assertThat(created.items()).extracting(item -> item.customerId())
                .containsExactly(firstCustomerId, secondCustomerId);
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("CAD"));
    }
}
