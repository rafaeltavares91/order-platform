package dev.study.orderplatform.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Order;

public record OrderResponse(
        UUID id,
        String customerId,
        String status,
        String currency,
        BigDecimal total,
        Instant createdAt,
        long version,
        List<Line> lines) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id(),
                order.customerId(),
                order.status().name(),
                order.total().currency().getCurrencyCode(),
                order.total().amount(),
                order.createdAt(),
                order.version(),
                order.lines().stream()
                        .map(line -> new Line(line.sku(), line.quantity(), line.unitPrice().amount()))
                        .toList());
    }

    public record Line(String sku, int quantity, BigDecimal unitPrice) {
    }
}
