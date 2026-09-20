package dev.study.orderplatform.domain.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import dev.study.orderplatform.domain.exception.CustomersNotFoundException;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItem;
import dev.study.orderplatform.domain.model.OrderItemAllocation;
import dev.study.orderplatform.domain.model.PaymentRequested;
import dev.study.orderplatform.domain.port.CustomerExistencePort;
import dev.study.orderplatform.domain.model.IdentifierGenerator;
import dev.study.orderplatform.domain.port.SaveOrderPort;

public class CreateOrderService {

    private final SaveOrderPort saveOrder;
    private final CustomerExistencePort customerExistence;
    private final IdentifierGenerator identifierGenerator;
    private final Clock clock;

    public CreateOrderService(
            SaveOrderPort saveOrder,
            CustomerExistencePort customerExistence,
            IdentifierGenerator identifierGenerator,
            Clock clock) {
        this.saveOrder = saveOrder;
        this.customerExistence = customerExistence;
        this.identifierGenerator = identifierGenerator;
        this.clock = clock;
    }

    public Order create(LocalDate creditDate, List<OrderItemAllocation> allocations) {
        validateCustomersExist(allocations);
        var orderId = identifierGenerator.nextId();
        var now = clock.instant();
        var items = allocations.stream()
                .map(allocation -> OrderItem.create(
                        identifierGenerator.nextId(), orderId, allocation.customerId(), allocation.amount(), now))
                .toList();
        var order = Order.create(orderId, creditDate, items, now);
        var paymentRequested = new PaymentRequested(
                identifierGenerator.nextId(), order.id(), order.totalAmount(), now);
        return saveOrder.save(order, paymentRequested);
    }

    private void validateCustomersExist(List<OrderItemAllocation> allocations) {
        var requestedCustomerIds = allocations.stream()
                .map(OrderItemAllocation::customerId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var existingCustomerIds = customerExistence.findExistingIds(requestedCustomerIds);
        var missingCustomerIds = new LinkedHashSet<>(requestedCustomerIds);
        missingCustomerIds.removeAll(existingCustomerIds);
        if (!missingCustomerIds.isEmpty()) {
            throw new CustomersNotFoundException(missingCustomerIds);
        }
    }
}
