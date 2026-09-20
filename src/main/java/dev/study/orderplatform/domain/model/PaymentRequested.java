package dev.study.orderplatform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PaymentRequested(UUID eventId, UUID orderId, Money totalAmount, Instant occurredAt) {

    public static final String EVENT_TYPE = "PaymentRequested.v1";

    public PaymentRequested {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
