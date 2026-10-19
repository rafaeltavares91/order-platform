package dev.study.orderplatform.domain.model;

import java.util.List;
import java.util.Objects;

public record PaymentContext(Order order, List<Customer> customers) {

    public PaymentContext {
        Objects.requireNonNull(order, "order must not be null");
        customers = List.copyOf(Objects.requireNonNull(customers, "customers must not be null"));
    }
}
