package dev.study.orderplatform.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.port.LoadOrderPort;
import dev.study.orderplatform.domain.port.SaveOrderPort;
import dev.study.orderplatform.persistence.entity.OrderEntity;
import dev.study.orderplatform.persistence.entity.OrderItemEntity;
import dev.study.orderplatform.persistence.repository.SpringDataOrderItemRepository;
import dev.study.orderplatform.persistence.repository.SpringDataOrderRepository;
import jakarta.persistence.EntityManager;

@Repository
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {

    private final EntityManager entityManager;
    private final SpringDataOrderRepository repository;
    private final SpringDataOrderItemRepository itemRepository;

    public OrderPersistenceAdapter(
            EntityManager entityManager,
            SpringDataOrderRepository repository,
            SpringDataOrderItemRepository itemRepository) {
        this.entityManager = entityManager;
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        var entity = OrderEntity.from(order);
        entityManager.persist(entity);
        for (var index = 0; index < order.items().size(); index++) {
            entityManager.persist(OrderItemEntity.from(order.items().get(index), index));
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        return repository.findById(id).map(entity -> {
            var items = itemRepository.findAllByOrderIdOrderByItemIndex(id).stream()
                    .map(OrderItemEntity::toDomain)
                    .toList();
            return entity.toDomain(items);
        });
    }
}
