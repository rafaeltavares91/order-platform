package dev.study.orderplatform.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import dev.study.orderplatform.application.CreateOrderService.CreateOrderCommand;
import dev.study.orderplatform.application.CreateOrderService.Line;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.port.SaveOrderPort;

class CreateOrderServiceTest {

    @Test
    void createsAnOrderUsingInjectedIdentityAndTime() {
        UUID id = UUID.fromString("01994d56-1200-7000-8000-000000000001");
        Instant now = Instant.parse("2026-09-14T12:00:00Z");
        var savedOrder = new AtomicReference<Order>();
        SaveOrderPort saveOrder = order -> {
            savedOrder.set(order);
            return order;
        };
        var service = new CreateOrderService(saveOrder, () -> id, Clock.fixed(now, ZoneOffset.UTC));

        Order created = service.create(new CreateOrderCommand(
                "customer-123",
                Currency.getInstance("CAD"),
                List.of(new Line("SKU-1", 2, new BigDecimal("10.2500")))));

        assertThat(created).isSameAs(savedOrder.get());
        assertThat(created.id()).isEqualTo(id);
        assertThat(created.createdAt()).isEqualTo(now);
        assertThat(created.total().amount()).isEqualByComparingTo("20.5000");
    }
}
