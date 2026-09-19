package dev.study.orderplatform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.study.orderplatform.domain.exception.OrderNotFoundException;
import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItem;
import dev.study.orderplatform.domain.port.LoadOrderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetOrderServiceTest {

    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");

    @Mock
    private LoadOrderPort loadOrder;

    private GetOrderService service;

    @BeforeEach
    void setUp() {
        service = new GetOrderService(loadOrder);
    }

    @Test
    void returnsTheOrderLoadedById() {
        var order = order();
        when(loadOrder.findById(ORDER_ID)).thenReturn(Optional.of(order));

        var loaded = service.getById(ORDER_ID);

        assertThat(loaded).isSameAs(order);
        verify(loadOrder).findById(ORDER_ID);
    }

    @Test
    void reportsWhenTheOrderDoesNotExist() {
        when(loadOrder.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(ORDER_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order %s was not found", ORDER_ID);
        verify(loadOrder).findById(ORDER_ID);
    }

    private static Order order() {
        var now = Instant.parse("2026-09-14T12:00:00Z");
        var item = OrderItem.create(
                UUID.fromString("01994d56-1200-7000-8000-000000000002"),
                ORDER_ID,
                UUID.fromString("01994d56-1200-7000-8000-000000000003"),
                new Money(new BigDecimal("20.5000"), Currency.getInstance("CAD")),
                now);
        return Order.create(ORDER_ID, LocalDate.parse("2026-09-15"), List.of(item), now);
    }
}
