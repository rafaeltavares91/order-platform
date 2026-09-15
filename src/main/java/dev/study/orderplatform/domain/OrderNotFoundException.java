package dev.study.orderplatform.domain;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderNotFoundException(UUID orderId) {
        super("Order %s was not found".formatted(orderId));
    }
}
