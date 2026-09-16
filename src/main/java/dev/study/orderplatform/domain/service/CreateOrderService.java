package dev.study.orderplatform.domain.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BiFunction;

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

    public Order create(BiFunction<UUID, Instant, Order> orderFactory) {
        var order = orderFactory.apply(orderIdGenerator.nextId(), clock.instant());
        return saveOrder.save(order);
    }
}
