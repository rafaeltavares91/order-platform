package dev.study.orderplatform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import dev.study.orderplatform.domain.exception.CustomersNotFoundException;
import org.junit.jupiter.api.Test;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItemAllocation;
import dev.study.orderplatform.domain.port.CustomerExistencePort;
import dev.study.orderplatform.domain.model.IdentifierGenerator;
import dev.study.orderplatform.domain.port.SaveOrderPort;

class CreateOrderServiceTest {

    @Test
    void createsAnOrderAndItsItemsUsingInjectedIdentityAndTime() {
        var orderId = UUID.fromString("01994d56-1200-7000-8000-000000000001");
        var firstItemId = UUID.fromString("01994d56-1200-7000-8000-000000000002");
        var secondItemId = UUID.fromString("01994d56-1200-7000-8000-000000000003");
        var firstCustomerId = UUID.fromString("01994d56-1200-7000-8000-000000000004");
        var secondCustomerId = UUID.fromString("01994d56-1200-7000-8000-000000000005");
        var now = Instant.parse("2026-09-14T12:00:00Z");
        var identifiers = new ArrayDeque<>(List.of(orderId, firstItemId, secondItemId));
        IdentifierGenerator identifierGenerator = identifiers::removeFirst;
        var savedOrder = new AtomicReference<Order>();
        SaveOrderPort saveOrder = order -> {
            savedOrder.set(order);
            return order;
        };
        var queriedCustomerIds = new AtomicReference<Set<UUID>>();
        CustomerExistencePort customerExistence = customerIds -> {
            queriedCustomerIds.set(customerIds);
            return customerIds;
        };
        var service = new CreateOrderService(
                saveOrder, customerExistence, identifierGenerator, Clock.fixed(now, ZoneOffset.UTC));
        var creditDate = LocalDate.parse("2026-09-15");
        var allocations = List.of(
                new OrderItemAllocation(firstCustomerId, money("20.5000")),
                new OrderItemAllocation(secondCustomerId, money("4.5000")));

        var created = service.create(creditDate, allocations);

        assertThat(created).isSameAs(savedOrder.get());
        assertThat(created.id()).isEqualTo(orderId);
        assertThat(created.createdAt()).isEqualTo(now);
        assertThat(created.updatedAt()).isEqualTo(now);
        assertThat(created.totalAmount()).isEqualTo(money("25.0000"));
        assertThat(created.items()).extracting(item -> item.id()).containsExactly(firstItemId, secondItemId);
        assertThat(created.items()).extracting(item -> item.customerId())
                .containsExactly(firstCustomerId, secondCustomerId);
        assertThat(queriedCustomerIds.get()).containsExactlyInAnyOrder(firstCustomerId, secondCustomerId);
    }

    @Test
    void reportsAllMissingCustomersBeforeGeneratingIdsOrSaving() {
        var existingCustomerId = UUID.fromString("01994d56-1200-7000-8000-000000000004");
        var firstMissingCustomerId = UUID.fromString("01994d56-1200-7000-8000-000000000005");
        var secondMissingCustomerId = UUID.fromString("01994d56-1200-7000-8000-000000000006");
        var saveCalled = new AtomicBoolean();
        SaveOrderPort saveOrder = order -> {
            saveCalled.set(true);
            return order;
        };
        CustomerExistencePort customerExistence = customerIds -> Set.of(existingCustomerId);
        IdentifierGenerator identifierGenerator = () -> {
            throw new AssertionError("identifiers must not be generated when a customer is missing");
        };
        var service = new CreateOrderService(
                saveOrder,
                customerExistence,
                identifierGenerator,
                Clock.fixed(Instant.parse("2026-09-14T12:00:00Z"), ZoneOffset.UTC));
        var creditDate = LocalDate.parse("2026-09-15");
        var allocations = List.of(
                new OrderItemAllocation(existingCustomerId, money("10.0000")),
                new OrderItemAllocation(firstMissingCustomerId, money("5.0000")),
                new OrderItemAllocation(secondMissingCustomerId, money("5.0000")),
                new OrderItemAllocation(firstMissingCustomerId, money("5.0000")));

        assertThatThrownBy(() -> service.create(creditDate, allocations))
                .isInstanceOfSatisfying(CustomersNotFoundException.class, exception ->
                        assertThat(exception.missingCustomerIds())
                                .containsExactlyElementsOf(new LinkedHashSet<>(
                                        List.of(firstMissingCustomerId, secondMissingCustomerId))));
        assertThat(saveCalled).isFalse();
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("CAD"));
    }
}
