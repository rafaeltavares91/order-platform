package dev.study.orderplatform.domain.port;

import java.util.Optional;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Order;

public interface LoadOrderPort {

    Optional<Order> findById(UUID id);
}
