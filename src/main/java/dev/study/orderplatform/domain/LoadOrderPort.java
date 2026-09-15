package dev.study.orderplatform.domain;

import java.util.Optional;
import java.util.UUID;

public interface LoadOrderPort {

    Optional<Order> findById(UUID id);
}
