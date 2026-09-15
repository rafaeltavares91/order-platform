package dev.study.orderplatform.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderLine;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class OrderPersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.6-alpine3.23");

    @Autowired
    private OrderPersistenceAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndReloadsAnOrderUsingTheProductionMigration() {
        Currency currency = Currency.getInstance("CAD");
        UUID id = UUID.fromString("01994d56-1200-7000-8000-000000000001");
        Order order = Order.place(
                id,
                "customer-123",
                currency,
                List.of(new OrderLine("SKU-1", 2, new Money(new BigDecimal("10.2500"), currency))),
                Instant.parse("2026-09-14T12:00:00Z"));

        adapter.save(order);
        entityManager.flush();
        entityManager.clear();

        var loaded = adapter.findById(id);
        assertThat(loaded).isPresent();
        assertThat(loaded.orElseThrow().id()).isEqualTo(order.id());
        assertThat(loaded.orElseThrow().customerId()).isEqualTo(order.customerId());
        assertThat(loaded.orElseThrow().total()).isEqualTo(order.total());
        assertThat(loaded.orElseThrow().lines()).isEqualTo(order.lines());
    }
}
