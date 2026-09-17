package dev.study.orderplatform.domain.service;

import java.time.Clock;

import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItem;
import dev.study.orderplatform.domain.port.IdentifierGenerator;
import dev.study.orderplatform.domain.port.SaveOrderPort;

public class CreateOrderService {

    private final SaveOrderPort saveOrder;
    private final IdentifierGenerator identifierGenerator;
    private final Clock clock;

    public CreateOrderService(SaveOrderPort saveOrder, IdentifierGenerator identifierGenerator, Clock clock) {
        this.saveOrder = saveOrder;
        this.identifierGenerator = identifierGenerator;
        this.clock = clock;
    }

    public Order create(CreateOrderCommand command) {
        var orderId = identifierGenerator.nextId();
        var now = clock.instant();
        var items = command.items().stream()
                .map(item -> OrderItem.create(
                        identifierGenerator.nextId(), orderId, item.customerId(), item.amount(), now))
                .toList();
        var order = Order.create(orderId, command.creditDate(), items, now);
        return saveOrder.save(order);
    }
}
