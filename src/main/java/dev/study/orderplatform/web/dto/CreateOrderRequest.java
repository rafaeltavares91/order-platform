package dev.study.orderplatform.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank @Size(max = 100) String customerId,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 15, fraction = 4)
                BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotNull LocalDate creditDate) {

    public Order toOrder(UUID id, Instant createdAt) {
        return Order.create(
                id,
                customerId,
                new Money(amount, Currency.getInstance(currency)),
                createdAt,
                creditDate);
    }
}
