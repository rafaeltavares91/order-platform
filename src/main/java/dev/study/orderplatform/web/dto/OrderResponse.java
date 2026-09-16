package dev.study.orderplatform.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Order;

public record OrderResponse(
        UUID id,
        String customerId,
        String status,
        String currency,
        BigDecimal amount,
        Instant createdAt,
        LocalDate creditDate,
        long version) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id(),
                order.customerId(),
                order.status().name(),
                order.amount().currency().getCurrencyCode(),
                order.amount().amount(),
                order.createdAt(),
                order.creditDate(),
                order.version());
    }
}
