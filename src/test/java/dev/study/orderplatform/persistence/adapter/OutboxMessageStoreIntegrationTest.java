package dev.study.orderplatform.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.OrderItemAllocation;
import dev.study.orderplatform.domain.model.PaymentRequested;
import dev.study.orderplatform.domain.service.CreateOrderService;
import dev.study.orderplatform.support.PostgreSqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OutboxMessageStoreIntegrationTest extends PostgreSqlIntegrationTest {

    private static final String CUSTOMER_ID = "01994d56-1200-7000-8000-000000000004";

    @Autowired
    private CreateOrderService createOrderService;

    @Autowired
    private OutboxMessageStore outbox;

    @Test
    void claimsAndMarksAnEventAsPublished() {
        insertCustomer(CUSTOMER_ID, "DOC-001", "Ada Lovelace");
        createOrderService.create(
                LocalDate.parse("2099-09-15"),
                List.of(new OrderItemAllocation(UUID.fromString(CUSTOMER_ID), money("20.0000"))));
        var now = Instant.now().plusSeconds(1);

        var claimed = outbox.claim(10, now, Duration.ofSeconds(30));

        assertThat(claimed).singleElement().satisfies(message -> {
            assertThat(message.eventType()).isEqualTo(PaymentRequested.EVENT_TYPE);
            assertThat(message.payload()).contains("\"eventType\": \"PaymentRequested.v1\"");
            outbox.markPublished(message.id(), now);
        });
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT status FROM outbox_messages", String.class))
                .isEqualTo("PUBLISHED");
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("CAD"));
    }
}
