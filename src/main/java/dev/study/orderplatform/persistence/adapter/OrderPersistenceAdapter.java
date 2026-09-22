package dev.study.orderplatform.persistence.adapter;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import dev.study.orderplatform.domain.model.OrderItem;
import dev.study.orderplatform.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.PaymentRequested;
import dev.study.orderplatform.domain.port.LoadOrderPort;
import dev.study.orderplatform.domain.port.SaveOrderPort;
import dev.study.orderplatform.persistence.entity.OrderEntity;
import dev.study.orderplatform.persistence.entity.OrderItemEntity;
import dev.study.orderplatform.persistence.repository.CustomerRepository;
import dev.study.orderplatform.persistence.repository.OrderItemRepository;
import dev.study.orderplatform.persistence.repository.OrderRepository;
import tools.jackson.databind.ObjectMapper;

@Repository
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {

    private final CustomerRepository customerRepository;
    private final OrderRepository repository;
    private final OrderItemRepository itemRepository;
    private final OutboxMessageStore outboxMessageStore;
    private final ObjectMapper objectMapper;

    public OrderPersistenceAdapter(
            CustomerRepository customerRepository,
            OrderRepository repository,
            OrderItemRepository itemRepository,
            OutboxMessageStore outboxMessageStore,
            ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.outboxMessageStore = outboxMessageStore;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Order save(Order order, PaymentRequested paymentRequested) {
        var customerPublicIds = order.items().stream()
                .map(OrderItem::customerId)
                .collect(Collectors.toSet());

        var customerIdsByPublicId = customerRepository.findAllByPublicIdIn(customerPublicIds).stream()
                .collect(Collectors.toMap(CustomerEntity::publicId, CustomerEntity::internalId));

        var entity = repository.save(OrderEntity.from(order));
        var itemEntities = new ArrayList<OrderItemEntity>(order.items().size());
        for (var index = 0; index < order.items().size(); index++) {
            var item = order.items().get(index);
            var customerId = customerIdsByPublicId.get(item.customerId());
            if (customerId == null) {
                throw new IllegalStateException("Customer %s no longer exists".formatted(item.customerId()));
            }
            itemEntities.add(OrderItemEntity.from(item, entity.internalId(), customerId, index));
        }
        itemRepository.saveAll(itemEntities);
        outboxMessageStore.enqueue(
                paymentRequested.eventId(),
                paymentRequested.orderId(),
                PaymentRequested.EVENT_TYPE,
                serialize(paymentRequested),
                paymentRequested.occurredAt());
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        return repository.findByPublicId(id).map(entity -> {
            var itemEntities = itemRepository.findAllByOrderIdOrderByItemIndex(entity.internalId());
            var customerInternalIds = itemEntities.stream()
                    .map(OrderItemEntity::customerInternalId)
                    .collect(Collectors.toSet());
            var customerPublicIdsByInternalId = customerRepository.findAllById(customerInternalIds).stream()
                    .collect(Collectors.toMap(CustomerEntity::internalId, CustomerEntity::publicId));
            var items = itemEntities.stream()
                    .map(item -> item.toDomain(
                            entity.publicId(), customerPublicIdsByInternalId.get(item.customerInternalId())))
                    .toList();
            return entity.toDomain(items);
        });
    }

    private String serialize(PaymentRequested event) {
        return objectMapper.writeValueAsString(new PaymentRequestedPayload(
                event.eventId(),
                PaymentRequested.EVENT_TYPE,
                event.occurredAt(),
                event.orderId(),
                event.totalAmount().amount().toPlainString(),
                event.totalAmount().currency().getCurrencyCode()));
    }

    private record PaymentRequestedPayload(
            UUID eventId,
            String eventType,
            java.time.Instant occurredAt,
            UUID orderId,
            String totalAmount,
            String currency) {
    }
}
