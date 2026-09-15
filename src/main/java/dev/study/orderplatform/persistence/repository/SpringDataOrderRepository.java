package dev.study.orderplatform.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.study.orderplatform.persistence.entity.OrderJpaEntity;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = "lines")
    Optional<OrderJpaEntity> findById(UUID id);
}
