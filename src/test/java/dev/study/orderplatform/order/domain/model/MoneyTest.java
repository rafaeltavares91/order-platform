package dev.study.orderplatform.order.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.Test;

class MoneyTest {

    private static final Currency CAD = Currency.getInstance("CAD");

    @Test
    void normalizesAmountsToTheDatabaseScale() {
        var money = new Money(new BigDecimal("12.34"), CAD);

        assertThat(money.amount()).isEqualByComparingTo("12.3400");
        assertThat(money.amount().scale()).isEqualTo(4);
    }

    @Test
    void refusesImplicitRounding() {
        assertThatThrownBy(() -> new Money(new BigDecimal("12.34567"), CAD))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Rounding necessary");
    }

    @Test
    void cannotAddDifferentCurrencies() {
        var cad = new Money(BigDecimal.ONE, CAD);
        var usd = new Money(BigDecimal.ONE, Currency.getInstance("USD"));

        assertThatThrownBy(() -> cad.add(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currencies");
    }
}
