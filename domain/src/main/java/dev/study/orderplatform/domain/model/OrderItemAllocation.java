package dev.study.orderplatform.domain.model;

import java.util.Objects;
import java.util.UUID;

public record OrderItemAllocation(UUID customerId, Money amount) {

    public OrderItemAllocation {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }
}
