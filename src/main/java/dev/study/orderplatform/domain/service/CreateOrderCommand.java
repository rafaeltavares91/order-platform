package dev.study.orderplatform.domain.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Money;

public record CreateOrderCommand(LocalDate creditDate, List<Item> items) {

    public CreateOrderCommand {
        Objects.requireNonNull(creditDate, "creditDate must not be null");
        Objects.requireNonNull(items, "items must not be null");
        items = List.copyOf(items);
    }

    public record Item(UUID customerId, Money amount) {

        public Item {
            Objects.requireNonNull(customerId, "customerId must not be null");
            Objects.requireNonNull(amount, "amount must not be null");
        }
    }
}
