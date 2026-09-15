package dev.study.orderplatform.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Currency;
import java.util.List;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderLine;
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
        var lines = command.lines().stream()
                .map(line -> new OrderLine(
                        line.sku(),
                        line.quantity(),
                        new Money(line.unitPrice(), command.currency())))
                .toList();

        var order = Order.place(
                orderIdGenerator.nextId(),
                command.customerId(),
                command.currency(),
                lines,
                clock.instant());
        return saveOrder.save(order);
    }

    public record CreateOrderCommand(String customerId, Currency currency, List<Line> lines) {

        public CreateOrderCommand {
            lines = List.copyOf(lines);
        }
    }

    public record Line(String sku, int quantity, BigDecimal unitPrice) {
    }
}
