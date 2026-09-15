package dev.study.orderplatform.order.application.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.study.orderplatform.order.application.port.in.CreateOrderUseCase;
import dev.study.orderplatform.order.application.port.out.OrderIdGenerator;
import dev.study.orderplatform.order.application.port.out.SaveOrderPort;
import dev.study.orderplatform.order.domain.model.Money;
import dev.study.orderplatform.order.domain.model.Order;
import dev.study.orderplatform.order.domain.model.OrderLine;

@Service
public class CreateOrderService implements CreateOrderUseCase {

    private final SaveOrderPort saveOrderPort;
    private final OrderIdGenerator orderIdGenerator;
    private final Clock clock;

    public CreateOrderService(SaveOrderPort saveOrderPort, OrderIdGenerator orderIdGenerator, Clock clock) {
        this.saveOrderPort = saveOrderPort;
        this.orderIdGenerator = orderIdGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
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
        return saveOrderPort.save(order);
    }
}
