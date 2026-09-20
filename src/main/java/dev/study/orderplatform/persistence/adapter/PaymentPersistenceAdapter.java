package dev.study.orderplatform.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.study.orderplatform.domain.model.Customer;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.PaymentContext;
import dev.study.orderplatform.domain.port.LoadPaymentContextPort;
import dev.study.orderplatform.domain.port.SavePaymentResultPort;
import dev.study.orderplatform.persistence.entity.CustomerEntity;
import dev.study.orderplatform.persistence.entity.OrderItemEntity;
import dev.study.orderplatform.persistence.repository.CustomerRepository;
import dev.study.orderplatform.persistence.repository.OrderItemRepository;
import dev.study.orderplatform.persistence.repository.OrderRepository;

@Repository
public class PaymentPersistenceAdapter implements LoadPaymentContextPort, SavePaymentResultPort {

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final CustomerRepository customerRepository;

    public PaymentPersistenceAdapter(
            OrderRepository orderRepository,
            OrderItemRepository itemRepository,
            CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public Optional<PaymentContext> findByOrderIdForUpdate(UUID orderId) {
        return orderRepository.findForUpdateByPublicId(orderId).map(orderEntity -> {
            var itemEntities = itemRepository.findAllByOrderIdOrderByItemIndex(orderEntity.internalId());
            var customerIds = itemEntities.stream()
                    .map(OrderItemEntity::customerInternalId)
                    .collect(Collectors.toSet());
            var customerEntities = customerRepository.findAllById(customerIds);
            var publicIdsByInternalId = customerEntities.stream()
                    .collect(Collectors.toMap(CustomerEntity::internalId, CustomerEntity::publicId));
            var items = itemEntities.stream()
                    .map(item -> item.toDomain(orderEntity.publicId(), publicIdsByInternalId.get(item.customerInternalId())))
                    .toList();
            var customerPublicIds = publicIdsByInternalId.values().stream().collect(Collectors.toSet());
            var lockedCustomers = customerRepository.findAllByPublicIdInForUpdate(customerPublicIds).stream()
                    .map(CustomerEntity::toDomain)
                    .toList();
            return new PaymentContext(orderEntity.toDomain(items), lockedCustomers);
        });
    }

    @Override
    @Transactional
    public void save(Order order, List<Customer> customers) {
        var orderEntity = orderRepository.findForUpdateByPublicId(order.id())
                .orElseThrow(() -> new IllegalStateException("Order disappeared while processing payment"));
        orderEntity.updateFrom(order);

        var itemsById = order.items().stream().collect(Collectors.toMap(item -> item.id(), Function.identity()));
        itemRepository.findAllByOrderIdOrderByItemIndex(orderEntity.internalId()).forEach(entity -> {
            var item = itemsById.get(entity.publicId());
            if (item == null) {
                throw new IllegalStateException("Order item disappeared while processing payment");
            }
            entity.updateFrom(item);
        });

        var customersById = customers.stream().collect(Collectors.toMap(Customer::id, Function.identity()));
        customerRepository.findAllByPublicIdInForUpdate(customersById.keySet()).forEach(entity -> {
            var customer = customersById.get(entity.publicId());
            entity.updateFrom(customer);
        });
    }
}
