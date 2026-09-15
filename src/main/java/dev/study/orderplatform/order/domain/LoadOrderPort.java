package dev.study.orderplatform.order.domain;

import java.util.Optional;
import java.util.UUID;

import dev.study.orderplatform.order.domain.model.Order;

public interface LoadOrderPort {

    Optional<Order> findById(UUID id);
}
