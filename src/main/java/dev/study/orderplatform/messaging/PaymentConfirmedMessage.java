package dev.study.orderplatform.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.PaymentConfirmation;

public record PaymentConfirmedMessage(
        UUID eventId,
        String eventType,
        UUID orderId,
        String paymentId,
        String paidAmount,
        String currency,
        Instant paidAt) {

    PaymentConfirmation toDomain() {
        if (!PaymentConfirmation.EVENT_TYPE.equals(eventType)) {
            throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
        return new PaymentConfirmation(
                eventId,
                orderId,
                paymentId,
                new Money(new BigDecimal(paidAmount), Currency.getInstance(currency)),
                paidAt);
    }
}
