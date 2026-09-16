package dev.study.orderplatform.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderTest {

    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");
    private static final Instant CREATED_AT = Instant.parse("2026-09-14T12:00:00Z");
    private static final LocalDate CREDIT_DATE = LocalDate.parse("2026-09-15");
    private static final Money AMOUNT = new Money(new BigDecimal("25.0000"), Currency.getInstance("CAD"));

    @Test
    void createsAnOrderWithItsInitialStatus() {
        Order order = Order.create(ORDER_ID, "customer-123", AMOUNT, CREATED_AT, CREDIT_DATE);

        assertThat(order.id()).isEqualTo(ORDER_ID);
        assertThat(order.customerId()).isEqualTo("customer-123");
        assertThat(order.amount()).isEqualTo(AMOUNT);
        assertThat(order.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.createdAt()).isEqualTo(CREATED_AT);
        assertThat(order.creditDate()).isEqualTo(CREDIT_DATE);
        assertThat(order.version()).isZero();
    }

    @Test
    void rejectsMissingRequiredValues() {
        assertThatNullPointerException()
                .isThrownBy(() -> Order.create(null, "customer-123", AMOUNT, CREATED_AT, CREDIT_DATE))
                .withMessage("id must not be null");
        assertThatThrownBy(() -> Order.create(ORDER_ID, " ", AMOUNT, CREATED_AT, CREDIT_DATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerId must not be blank");
        assertThatNullPointerException()
                .isThrownBy(() -> Order.create(ORDER_ID, "customer-123", null, CREATED_AT, CREDIT_DATE))
                .withMessage("amount must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> Order.create(ORDER_ID, "customer-123", AMOUNT, null, CREDIT_DATE))
                .withMessage("createdAt must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> Order.create(ORDER_ID, "customer-123", AMOUNT, CREATED_AT, null))
                .withMessage("creditDate must not be null");
    }

    @Test
    void rejectsANonPositiveAmount() {
        Money zero = Money.zero(Currency.getInstance("CAD"));

        assertThatThrownBy(() -> Order.create(ORDER_ID, "customer-123", zero, CREATED_AT, CREDIT_DATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be greater than zero");
    }

    @Test
    void rejectsACreditDateBeforeTheUtcCreationDate() {
        assertThatThrownBy(() -> Order.create(
                        ORDER_ID, "customer-123", AMOUNT, CREATED_AT, LocalDate.parse("2026-09-13")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("creditDate must not be before the creation date");
    }
}
