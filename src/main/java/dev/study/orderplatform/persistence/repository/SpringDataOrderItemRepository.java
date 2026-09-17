package dev.study.orderplatform.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.study.orderplatform.persistence.entity.OrderItemEntity;

public interface SpringDataOrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {

    List<OrderItemEntity> findAllByOrderIdOrderByItemIndex(UUID orderId);
}
