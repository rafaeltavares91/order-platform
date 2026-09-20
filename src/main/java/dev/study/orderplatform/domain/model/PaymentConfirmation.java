package dev.study.orderplatform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PaymentConfirmation(
        UUID eventId,
        UUID orderId,
        String paymentId,
        Money paidAmount,
        Instant paidAt) {

    public static final String EVENT_TYPE = "PaymentConfirmed.v1";

    public PaymentConfirmation {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId must not be blank");
        }
        Objects.requireNonNull(paidAmount, "paidAmount must not be null");
        Objects.requireNonNull(paidAt, "paidAt must not be null");
    }
}
