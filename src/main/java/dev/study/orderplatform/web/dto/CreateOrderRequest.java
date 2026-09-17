package dev.study.orderplatform.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.OrderItemAllocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateOrderRequest(
        @NotNull LocalDate creditDate,
        @NotEmpty List<@NotNull @Valid Item> items) {

    public List<OrderItemAllocation> toDomain() {
        return items.stream().map(Item::toDomain).toList();
    }

    public record Item(
            @NotNull UUID customerId,
            @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 15, fraction = 4)
                    BigDecimal amount,
            @NotNull @Pattern(regexp = "^[A-Z]{3}$") String currency) {

        private OrderItemAllocation toDomain() {
            return new OrderItemAllocation(
                    customerId,
                    new Money(amount, Currency.getInstance(currency)));
        }
    }
}
