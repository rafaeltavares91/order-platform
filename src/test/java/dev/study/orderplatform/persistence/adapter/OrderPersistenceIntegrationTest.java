package dev.study.orderplatform.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import dev.study.orderplatform.domain.model.Customer;
import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItem;
import dev.study.orderplatform.persistence.entity.CustomerEntity;
import dev.study.orderplatform.persistence.repository.SpringDataCustomerRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class OrderPersistenceIntegrationTest {

    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");
    private static final UUID FIRST_ITEM_ID = UUID.fromString("01994d56-1200-7000-8000-000000000002");
    private static final UUID SECOND_ITEM_ID = UUID.fromString("01994d56-1200-7000-8000-000000000003");
    private static final UUID FIRST_CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000004");
    private static final UUID SECOND_CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000005");
    private static final Instant CREATED_AT = Instant.parse("2026-09-14T12:00:00Z");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.6-alpine3.23");

    @Autowired
    private OrderPersistenceAdapter adapter;

    @Autowired
    private SpringDataCustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndReloadsAnOrderWithItsItemsAndCustomers() {
        persistCustomers();
        var firstItem = OrderItem.create(
                FIRST_ITEM_ID, ORDER_ID, FIRST_CUSTOMER_ID, money("20.5000"), CREATED_AT);
        var secondItem = OrderItem.create(
                SECOND_ITEM_ID, ORDER_ID, SECOND_CUSTOMER_ID, money("4.5000"), CREATED_AT);
        var order = Order.create(
                ORDER_ID,
                LocalDate.parse("2026-09-15"),
                List.of(firstItem, secondItem),
                CREATED_AT);

        adapter.save(order);
        entityManager.flush();
        entityManager.clear();

        var loaded = adapter.findById(ORDER_ID).orElseThrow();
        assertThat(loaded.id()).isEqualTo(order.id());
        assertThat(loaded.totalAmount()).isEqualTo(money("25.0000"));
        assertThat(loaded.items()).extracting(OrderItem::id).containsExactly(FIRST_ITEM_ID, SECOND_ITEM_ID);
        assertThat(loaded.items()).extracting(OrderItem::customerId)
                .containsExactly(FIRST_CUSTOMER_ID, SECOND_CUSTOMER_ID);
        assertThat(loaded.items()).extracting(OrderItem::amount)
                .containsExactly(money("20.5000"), money("4.5000"));
        assertThat(loaded.updatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void mapsCustomersIndependentlyFromOrderPersistence() {
        var customer = Customer.create(
                FIRST_CUSTOMER_ID,
                "DOC-001",
                "Ada Lovelace",
                Currency.getInstance("CAD"),
                CREATED_AT);
        customerRepository.saveAndFlush(CustomerEntity.from(customer));
        entityManager.clear();

        var loaded = customerRepository.findByDocument("DOC-001").orElseThrow().toDomain();

        assertThat(loaded.id()).isEqualTo(FIRST_CUSTOMER_ID);
        assertThat(loaded.document()).isEqualTo("DOC-001");
        assertThat(loaded.name()).isEqualTo("Ada Lovelace");
        assertThat(loaded.balance()).isEqualTo(Money.zero(Currency.getInstance("CAD")));
    }

    @Test
    void rejectsCustomersWithTheSameBusinessDocument() {
        var firstCustomer = Customer.create(
                FIRST_CUSTOMER_ID,
                "DOC-001",
                "Ada Lovelace",
                Currency.getInstance("CAD"),
                CREATED_AT);
        var secondCustomer = Customer.create(
                SECOND_CUSTOMER_ID,
                "DOC-001",
                "Grace Hopper",
                Currency.getInstance("CAD"),
                CREATED_AT);
        customerRepository.saveAndFlush(CustomerEntity.from(firstCustomer));

        assertThatThrownBy(() -> customerRepository.saveAndFlush(CustomerEntity.from(secondCustomer)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void persistCustomers() {
        var firstCustomer = Customer.create(
                FIRST_CUSTOMER_ID,
                "DOC-001",
                "Ada Lovelace",
                Currency.getInstance("CAD"),
                CREATED_AT);
        var secondCustomer = Customer.create(
                SECOND_CUSTOMER_ID,
                "DOC-002",
                "Grace Hopper",
                Currency.getInstance("CAD"),
                CREATED_AT);
        entityManager.persist(CustomerEntity.from(firstCustomer));
        entityManager.persist(CustomerEntity.from(secondCustomer));
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("CAD"));
    }
}
