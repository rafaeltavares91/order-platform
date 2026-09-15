package dev.study.orderplatform.order.application.port.out;

import java.util.UUID;

public interface OrderIdGenerator {

    UUID nextId();
}
