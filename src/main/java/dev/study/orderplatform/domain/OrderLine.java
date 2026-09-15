package dev.study.orderplatform.domain;

import java.util.Objects;

public record OrderLine(String sku, int quantity, Money unitPrice) {

    public OrderLine {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (sku.length() > 100) {
            throw new IllegalArgumentException("sku must not exceed 100 characters");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (unitPrice.amount().signum() <= 0) {
            throw new IllegalArgumentException("unitPrice must be greater than zero");
        }
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}
