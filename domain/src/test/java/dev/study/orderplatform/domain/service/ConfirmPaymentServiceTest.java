package dev.study.orderplatform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.study.orderplatform.domain.exception.PaymentAmountMismatchException;
import dev.study.orderplatform.domain.model.Customer;
import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItem;
import dev.study.orderplatform.domain.model.OrderStatus;
import dev.study.orderplatform.domain.model.PaymentConfirmation;
import dev.study.orderplatform.domain.model.PaymentContext;
import dev.study.orderplatform.domain.port.LoadPaymentContextPort;
import dev.study.orderplatform.domain.port.SavePaymentResultPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfirmPaymentServiceTest {

    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000004");
    private static final Instant CREATED_AT = Instant.parse("2026-09-14T12:00:00Z");
    private static final Instant PAID_AT = Instant.parse("2026-09-15T12:00:00Z");

    @Mock
    private LoadPaymentContextPort loadPaymentContext;

    @Mock
    private SavePaymentResultPort savePaymentResult;

    private ConfirmPaymentService service;

    @BeforeEach
    void setUp() {
        service = new ConfirmPaymentService(
                loadPaymentContext,
                savePaymentResult,
                Clock.fixed(PAID_AT, ZoneOffset.UTC));
    }

    @Test
    void creditsTheOrderAndCustomersAtomically() {
        when(loadPaymentContext.findByOrderIdForUpdate(ORDER_ID)).thenReturn(Optional.of(context()));

        service.confirm(confirmation("20.0000"));

        var order = ArgumentCaptor.forClass(Order.class);
        @SuppressWarnings("unchecked")
        var customers = ArgumentCaptor.forClass((Class<List<dev.study.orderplatform.domain.model.Customer>>) (Class<?>) List.class);
        verify(savePaymentResult).save(order.capture(), customers.capture());
        assertThat(order.getValue().status()).isEqualTo(OrderStatus.CREDITED);
        assertThat(order.getValue().paymentId()).isEqualTo("PAY-001");
        assertThat(customers.getValue()).singleElement()
                .extracting(customer -> customer.balance().amount())
                .isEqualTo(new BigDecimal("20.0000"));
    }

    @Test
    void rejectsAPaymentWithADifferentAmount() {
        when(loadPaymentContext.findByOrderIdForUpdate(ORDER_ID)).thenReturn(Optional.of(context()));

        assertThatThrownBy(() -> service.confirm(confirmation("19.0000")))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(savePaymentResult, never()).save(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private static PaymentContext context() {
        var item = OrderItem.create(UUID.randomUUID(), ORDER_ID, CUSTOMER_ID, money("20.0000"), CREATED_AT);
        var order = Order.create(ORDER_ID, LocalDate.parse("2026-09-15"), List.of(item), CREATED_AT);
        var customer = Customer.create(CUSTOMER_ID, "DOC-001", "Ada", Currency.getInstance("CAD"), CREATED_AT);
        return new PaymentContext(order, List.of(customer));
    }

    private static PaymentConfirmation confirmation(String amount) {
        return new PaymentConfirmation(
                UUID.randomUUID(), ORDER_ID, "PAY-001", money(amount), PAID_AT);
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("CAD"));
    }
}
