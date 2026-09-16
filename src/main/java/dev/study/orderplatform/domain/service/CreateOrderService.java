package dev.study.orderplatform.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Currency;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.port.OrderIdGenerator;
import dev.study.orderplatform.domain.port.SaveOrderPort;

public class CreateOrderService {

    private final SaveOrderPort saveOrder;
    private final OrderIdGenerator orderIdGenerator;
    private final Clock clock;

    public CreateOrderService(SaveOrderPort saveOrder, OrderIdGenerator orderIdGenerator, Clock clock) {
        this.saveOrder = saveOrder;
        this.orderIdGenerator = orderIdGenerator;
        this.clock = clock;
    }

    public Order create(CreateOrderCommand command) {
        var order = Order.create(
                orderIdGenerator.nextId(),
                command.customerId(),
                new Money(command.amount(), command.currency()),
                clock.instant(),
                command.creditDate());
        return saveOrder.save(order);
    }

    public record CreateOrderCommand(
            String customerId, BigDecimal amount, Currency currency, LocalDate creditDate) {
    }
}
