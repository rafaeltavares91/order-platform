package dev.study.orderplatform.domain;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Currency;
import java.util.List;

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
