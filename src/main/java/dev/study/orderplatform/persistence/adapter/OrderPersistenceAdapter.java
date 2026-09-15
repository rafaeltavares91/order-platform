package dev.study.orderplatform.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.port.LoadOrderPort;
import dev.study.orderplatform.domain.port.SaveOrderPort;
import dev.study.orderplatform.persistence.entity.OrderJpaEntity;
import dev.study.orderplatform.persistence.repository.SpringDataOrderRepository;
import jakarta.persistence.EntityManager;

@Repository
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {

    private final EntityManager entityManager;
    private final SpringDataOrderRepository repository;

    public OrderPersistenceAdapter(EntityManager entityManager, SpringDataOrderRepository repository) {
        this.entityManager = entityManager;
        this.repository = repository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        var entity = OrderJpaEntity.from(order);
        entityManager.persist(entity);
        return entity.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        return repository.findById(id).map(OrderJpaEntity::toDomain);
    }
}
