package dev.study.orderplatform.persistence.adapter;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.port.LoadOrderPort;
import dev.study.orderplatform.domain.port.SaveOrderPort;
import dev.study.orderplatform.persistence.entity.OrderEntity;
import dev.study.orderplatform.persistence.entity.OrderItemEntity;
import dev.study.orderplatform.persistence.repository.CustomerRepository;
import dev.study.orderplatform.persistence.repository.OrderItemRepository;
import dev.study.orderplatform.persistence.repository.OrderRepository;
import jakarta.persistence.EntityManager;

@Repository
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {

    private final EntityManager entityManager;
    private final CustomerRepository customerRepository;
    private final OrderRepository repository;
    private final OrderItemRepository itemRepository;

    public OrderPersistenceAdapter(
            EntityManager entityManager,
            CustomerRepository customerRepository,
            OrderRepository repository,
            OrderItemRepository itemRepository) {
        this.entityManager = entityManager;
        this.customerRepository = customerRepository;
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        var customerPublicIds = order.items().stream()
                .map(item -> item.customerId())
                .collect(Collectors.toSet());
        var customerIdsByPublicId = customerRepository.findAllByPublicIdIn(customerPublicIds).stream()
                .collect(Collectors.toMap(customer -> customer.publicId(), customer -> customer.internalId()));

        var entity = OrderEntity.from(order);
        entityManager.persist(entity);
        for (var index = 0; index < order.items().size(); index++) {
            var item = order.items().get(index);
            var customerId = customerIdsByPublicId.get(item.customerId());
            if (customerId == null) {
                throw new IllegalStateException("Customer %s no longer exists".formatted(item.customerId()));
            }
            entityManager.persist(OrderItemEntity.from(item, entity.internalId(), customerId, index));
        }
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
                    .collect(Collectors.toMap(customer -> customer.internalId(), customer -> customer.publicId()));
            var items = itemEntities.stream()
                    .map(item -> item.toDomain(
                            entity.publicId(), customerPublicIdsByInternalId.get(item.customerInternalId())))
                    .toList();
            return entity.toDomain(items);
        });
    }
}
