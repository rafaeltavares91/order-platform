package dev.study.orderplatform.order.adapter.in.web;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import dev.study.orderplatform.order.domain.CreateOrderService;
import dev.study.orderplatform.order.domain.CreateOrderService.CreateOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank @Size(max = 100) String customerId,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotEmpty List<@Valid Line> lines) {

    CreateOrderCommand toCommand() {
        return new CreateOrderCommand(
                customerId,
                Currency.getInstance(currency),
                lines.stream().map(Line::toCommand).toList());
    }

    public record Line(
            @NotBlank @Size(max = 100) String sku,
            @Positive @Max(1_000_000) int quantity,
            @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 15, fraction = 4)
                    BigDecimal unitPrice) {

        CreateOrderService.Line toCommand() {
            return new CreateOrderService.Line(sku, quantity, unitPrice);
        }
    }
}
