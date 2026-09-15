package dev.study.orderplatform.order.application.port.out;

import java.util.Optional;
import java.util.UUID;

import dev.study.orderplatform.order.domain.model.Order;

public interface LoadOrderPort {

    Optional<Order> findById(UUID id);
}
