package dev.study.orderplatform.domain.port;

import java.util.UUID;

public interface OrderIdGenerator {

    UUID nextId();
}
