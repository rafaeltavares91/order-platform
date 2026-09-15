package dev.study.orderplatform.domain;

import java.util.UUID;

public interface OrderIdGenerator {

    UUID nextId();
}
