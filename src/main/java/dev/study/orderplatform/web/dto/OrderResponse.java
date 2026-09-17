package dev.study.orderplatform.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItem;

public record OrderResponse(
        UUID id,
        String status,
        LocalDate creditDate,
        BigDecimal totalAmount,
        String currency,
        List<Item> items,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id(),
                order.status().name(),
                order.creditDate(),
                order.totalAmount().amount(),
                order.totalAmount().currency().getCurrencyCode(),
                order.items().stream().map(Item::from).toList(),
                order.createdAt(),
                order.updatedAt(),
                order.version());
    }

    public record Item(
            UUID id,
            UUID customerId,
            BigDecimal amount,
            String currency,
            String status,
            Instant createdAt,
            Instant updatedAt,
            long version) {

        private static Item from(OrderItem item) {
            return new Item(
                    item.id(),
                    item.customerId(),
                    item.amount().amount(),
                    item.amount().currency().getCurrencyCode(),
                    item.status().name(),
                    item.createdAt(),
                    item.updatedAt(),
                    item.version());
        }
    }
}
