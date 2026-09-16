package dev.study.orderplatform.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import dev.study.orderplatform.application.CreateOrderService.CreateOrderCommand;
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

    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(customerId, amount, Currency.getInstance(currency), creditDate);
    }
}
