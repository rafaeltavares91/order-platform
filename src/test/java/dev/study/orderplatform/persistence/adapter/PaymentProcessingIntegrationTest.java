package dev.study.orderplatform.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.OrderItemAllocation;
import dev.study.orderplatform.domain.model.PaymentConfirmation;
import dev.study.orderplatform.domain.service.ConfirmPaymentService;
import dev.study.orderplatform.domain.service.CreateOrderService;
import dev.study.orderplatform.support.PostgreSqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class PaymentProcessingIntegrationTest extends PostgreSqlIntegrationTest {

    private static final String FIRST_CUSTOMER_ID = "01994d56-1200-7000-8000-000000000004";
    private static final String SECOND_CUSTOMER_ID = "01994d56-1200-7000-8000-000000000005";

    @Autowired
    private CreateOrderService createOrderService;

    @Autowired
    private ConfirmPaymentService confirmPaymentService;

    @Autowired
    private InboxMessageStore inbox;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void processesAPaymentOnceAndCreditsEveryCustomer() {
        insertCustomer(FIRST_CUSTOMER_ID, "DOC-001", "Ada Lovelace");
        insertCustomer(SECOND_CUSTOMER_ID, "DOC-002", "Grace Hopper");
        var order = createOrderService.create(
                LocalDate.parse("2099-09-15"),
                List.of(
                        new OrderItemAllocation(UUID.fromString(FIRST_CUSTOMER_ID), money("20.5000")),
                        new OrderItemAllocation(UUID.fromString(SECOND_CUSTOMER_ID), money("4.5000"))));
        var confirmation = new PaymentConfirmation(
                UUID.randomUUID(),
                order.id(),
                "PAY-001",
                money("25.0000"),
                Instant.parse("2026-09-19T12:00:00Z"));

        process(confirmation);
        process(confirmation);

        assertThat(jdbcTemplate.queryForObject(
                        "SELECT status FROM orders WHERE public_id = ?::uuid", String.class, order.id().toString()))
                .isEqualTo("CREDITED");
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT payment_id FROM orders WHERE public_id = ?::uuid", String.class, order.id().toString()))
                .isEqualTo("PAY-001");
        assertThat(jdbcTemplate.queryForList(
                        "SELECT balance FROM customers ORDER BY document", BigDecimal.class))
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("20.5000"), new BigDecimal("4.5000"));
        assertThat(jdbcTemplate.queryForList(
                        "SELECT status FROM order_items ORDER BY item_index", String.class))
                .containsExactly("CREDITED", "CREDITED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM inbox_messages", Long.class)).isEqualTo(1L);
    }

    private void process(PaymentConfirmation confirmation) {
        transactionTemplate.executeWithoutResult(status -> {
            if (inbox.register(confirmation.eventId(), PaymentConfirmation.EVENT_TYPE, Instant.now())) {
                confirmPaymentService.confirm(confirmation);
            }
        });
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("CAD"));
    }
}
