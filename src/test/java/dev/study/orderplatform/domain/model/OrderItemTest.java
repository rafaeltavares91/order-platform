package dev.study.orderplatform.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderItemTest {

    private static final UUID ITEM_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");
    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000002");
    private static final UUID CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000003");
    private static final Instant CREATED_AT = Instant.parse("2026-09-14T12:00:00Z");

    @Test
    void createsAPendingItemAssociatedWithItsOrderAndCustomer() {
        var amount = new Money(new BigDecimal("20.5000"), Currency.getInstance("CAD"));

        var item = OrderItem.create(ITEM_ID, ORDER_ID, CUSTOMER_ID, amount, CREATED_AT);

        assertThat(item.id()).isEqualTo(ITEM_ID);
        assertThat(item.orderId()).isEqualTo(ORDER_ID);
        assertThat(item.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(item.amount()).isEqualTo(amount);
        assertThat(item.status()).isEqualTo(OrderItemStatus.PENDING);
        assertThat(item.createdAt()).isEqualTo(CREATED_AT);
        assertThat(item.updatedAt()).isEqualTo(CREATED_AT);
        assertThat(item.version()).isZero();
    }

    @Test
    void rejectsANonPositiveAmount() {
        var zero = Money.zero(Currency.getInstance("CAD"));

        assertThatThrownBy(() -> OrderItem.create(ITEM_ID, ORDER_ID, CUSTOMER_ID, zero, CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be greater than zero");
    }
}
