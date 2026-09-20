package dev.study.orderplatform.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderTest {

    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");
    private static final UUID FIRST_ITEM_ID = UUID.fromString("01994d56-1200-7000-8000-000000000002");
    private static final UUID SECOND_ITEM_ID = UUID.fromString("01994d56-1200-7000-8000-000000000003");
    private static final UUID FIRST_CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000004");
    private static final UUID SECOND_CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000005");
    private static final Instant CREATED_AT = Instant.parse("2026-09-14T12:00:00Z");
    private static final LocalDate CREDIT_DATE = LocalDate.parse("2026-09-15");
    private static final Currency CAD = Currency.getInstance("CAD");

    @Test
    void createsAnOrderWithMultipleItemsAndCalculatesItsTotal() {
        var firstItem = item(FIRST_ITEM_ID, FIRST_CUSTOMER_ID, "20.5000");
        var secondItem = item(SECOND_ITEM_ID, SECOND_CUSTOMER_ID, "4.5000");

        var order = Order.create(ORDER_ID, CREDIT_DATE, List.of(firstItem, secondItem), CREATED_AT);

        assertThat(order.id()).isEqualTo(ORDER_ID);
        assertThat(order.status()).isEqualTo(OrderStatus.WAITING_PAYMENT);
        assertThat(order.totalAmount()).isEqualTo(money("25.0000"));
        assertThat(order.items()).containsExactly(firstItem, secondItem);
        assertThat(order.createdAt()).isEqualTo(CREATED_AT);
        assertThat(order.updatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void rejectsAnOrderWithoutItems() {
        assertThatThrownBy(() -> Order.create(ORDER_ID, CREDIT_DATE, List.of(), CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items must not be empty");
    }

    @Test
    void rejectsAnItemAssociatedWithAnotherOrder() {
        var anotherOrderId = UUID.fromString("01994d56-1200-7000-8000-000000000099");
        var item = OrderItem.create(FIRST_ITEM_ID, anotherOrderId, FIRST_CUSTOMER_ID, money("20.0000"), CREATED_AT);

        assertThatThrownBy(() -> Order.create(ORDER_ID, CREDIT_DATE, List.of(item), CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("every item must belong to the order");
    }

    @Test
    void rejectsItemsWithDifferentCurrencies() {
        var cadItem = item(FIRST_ITEM_ID, FIRST_CUSTOMER_ID, "20.0000");
        var usdItem = OrderItem.create(
                SECOND_ITEM_ID,
                ORDER_ID,
                SECOND_CUSTOMER_ID,
                new Money(new BigDecimal("5.0000"), Currency.getInstance("USD")),
                CREATED_AT);

        assertThatThrownBy(() -> Order.create(ORDER_ID, CREDIT_DATE, List.of(cadItem, usdItem), CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("money currencies must match");
    }

    @Test
    void rejectsAPersistedTotalThatDoesNotMatchTheItems() {
        var item = item(FIRST_ITEM_ID, FIRST_CUSTOMER_ID, "20.0000");

        assertThatThrownBy(() -> Order.rehydrate(
                        ORDER_ID,
                        OrderStatus.CREATED,
                        CREDIT_DATE,
                        money("99.0000"),
                        List.of(item),
                        CREATED_AT,
                        CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("totalAmount must equal the sum of item amounts");
    }

    @Test
    void rejectsACreditDateBeforeTheUtcCreationDate() {
        var item = item(FIRST_ITEM_ID, FIRST_CUSTOMER_ID, "20.0000");

        assertThatThrownBy(() -> Order.create(
                        ORDER_ID, LocalDate.parse("2026-09-13"), List.of(item), CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("creditDate must not be before the creation date");
    }

    private static OrderItem item(UUID itemId, UUID customerId, String amount) {
        return OrderItem.create(itemId, ORDER_ID, customerId, money(amount), CREATED_AT);
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), CAD);
    }
}
