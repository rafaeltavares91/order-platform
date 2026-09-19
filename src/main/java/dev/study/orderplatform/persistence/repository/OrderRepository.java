package dev.study.orderplatform.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.study.orderplatform.persistence.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
}
