package dev.study.orderplatform.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CustomerTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");
    private static final Instant CREATED_AT = Instant.parse("2026-09-14T12:00:00Z");

    @Test
    void createsACustomerWithAZeroBalance() {
        var customer = Customer.create(CUSTOMER_ID, "123456789", "Ada Lovelace", Currency.getInstance("CAD"), CREATED_AT);

        assertThat(customer.id()).isEqualTo(CUSTOMER_ID);
        assertThat(customer.document()).isEqualTo("123456789");
        assertThat(customer.name()).isEqualTo("Ada Lovelace");
        assertThat(customer.balance()).isEqualTo(Money.zero(Currency.getInstance("CAD")));
        assertThat(customer.createdAt()).isEqualTo(CREATED_AT);
        assertThat(customer.updatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void rejectsABlankBusinessDocument() {
        assertThatThrownBy(() -> Customer.create(
                        CUSTOMER_ID, " ", "Ada Lovelace", Currency.getInstance("CAD"), CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("document must not be blank");
    }
}
