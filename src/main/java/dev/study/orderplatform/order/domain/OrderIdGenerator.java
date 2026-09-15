package dev.study.orderplatform.order.domain;

import java.util.UUID;

public interface OrderIdGenerator {

    UUID nextId();
}
