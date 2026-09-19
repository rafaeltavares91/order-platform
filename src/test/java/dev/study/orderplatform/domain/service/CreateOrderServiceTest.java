package dev.study.orderplatform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import dev.study.orderplatform.domain.exception.CustomersNotFoundException;
import dev.study.orderplatform.domain.model.IdentifierGenerator;
import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItemAllocation;
import dev.study.orderplatform.domain.model.OrderItemStatus;
import dev.study.orderplatform.domain.model.OrderStatus;
import dev.study.orderplatform.domain.port.CustomerExistencePort;
import dev.study.orderplatform.domain.port.SaveOrderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");
    private static final UUID FIRST_ITEM_ID = UUID.fromString("01994d56-1200-7000-8000-000000000002");
    private static final UUID SECOND_ITEM_ID = UUID.fromString("01994d56-1200-7000-8000-000000000003");
    private static final UUID FIRST_CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000004");
    private static final UUID SECOND_CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000005");
    private static final Instant NOW = Instant.parse("2026-09-14T12:00:00Z");
    private static final LocalDate CREDIT_DATE = LocalDate.parse("2026-09-15");

    @Mock
    private SaveOrderPort saveOrder;

    @Mock
    private CustomerExistencePort customerExistence;

    @Mock
    private IdentifierGenerator identifierGenerator;

    @Mock
    private Clock clock;

    private CreateOrderService service;

    @BeforeEach
    void setUp() {
        service = new CreateOrderService(saveOrder, customerExistence, identifierGenerator, clock);
    }

    @Test
    void createsAndSavesAnOrderUsingGeneratedIdentifiersAndCurrentTime() {
        var allocations = List.of(
                new OrderItemAllocation(FIRST_CUSTOMER_ID, money("20.5000")),
                new OrderItemAllocation(SECOND_CUSTOMER_ID, money("4.5000")));
        when(customerExistence.findExistingIds(Set.of(FIRST_CUSTOMER_ID, SECOND_CUSTOMER_ID)))
                .thenReturn(Set.of(FIRST_CUSTOMER_ID, SECOND_CUSTOMER_ID));
        when(identifierGenerator.nextId()).thenReturn(ORDER_ID, FIRST_ITEM_ID, SECOND_ITEM_ID);
        when(clock.instant()).thenReturn(NOW);
        when(saveOrder.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(CREDIT_DATE, allocations);

        assertThat(created.id()).isEqualTo(ORDER_ID);
        assertThat(created.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(created.creditDate()).isEqualTo(CREDIT_DATE);
        assertThat(created.totalAmount()).isEqualTo(money("25.0000"));
        assertThat(created.createdAt()).isEqualTo(NOW);
        assertThat(created.updatedAt()).isEqualTo(NOW);
        assertThat(created.items()).satisfiesExactly(
                first -> {
                    assertThat(first.id()).isEqualTo(FIRST_ITEM_ID);
                    assertThat(first.customerId()).isEqualTo(FIRST_CUSTOMER_ID);
                    assertThat(first.amount()).isEqualTo(money("20.5000"));
                    assertThat(first.status()).isEqualTo(OrderItemStatus.PENDING);
                    assertThat(first.createdAt()).isEqualTo(NOW);
                },
                second -> {
                    assertThat(second.id()).isEqualTo(SECOND_ITEM_ID);
                    assertThat(second.customerId()).isEqualTo(SECOND_CUSTOMER_ID);
                    assertThat(second.amount()).isEqualTo(money("4.5000"));
                    assertThat(second.status()).isEqualTo(OrderItemStatus.PENDING);
                    assertThat(second.createdAt()).isEqualTo(NOW);
                });

        var savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(saveOrder).save(savedOrder.capture());
        assertThat(savedOrder.getValue()).isSameAs(created);
        verify(customerExistence).findExistingIds(Set.of(FIRST_CUSTOMER_ID, SECOND_CUSTOMER_ID));
        verify(clock).instant();

        verify(identifierGenerator, times(3)).nextId();
    }

    @Test
    void checksEachCustomerOnlyOnceWhenSeveralItemsReferenceTheSameCustomer() {
        var allocations = List.of(
                new OrderItemAllocation(FIRST_CUSTOMER_ID, money("10.0000")),
                new OrderItemAllocation(FIRST_CUSTOMER_ID, money("5.0000")));
        when(customerExistence.findExistingIds(Set.of(FIRST_CUSTOMER_ID))).thenReturn(Set.of(FIRST_CUSTOMER_ID));
        when(identifierGenerator.nextId()).thenReturn(ORDER_ID, FIRST_ITEM_ID, SECOND_ITEM_ID);
        when(clock.instant()).thenReturn(NOW);
        when(saveOrder.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(CREDIT_DATE, allocations);

        assertThat(created.items()).hasSize(2);
        verify(customerExistence).findExistingIds(Set.of(FIRST_CUSTOMER_ID));
    }

    @Test
    void reportsAllMissingCustomersBeforeGeneratingIdentifiersOrSaving() {
        var firstMissingCustomerId = UUID.fromString("01994d56-1200-7000-8000-000000000006");
        var secondMissingCustomerId = UUID.fromString("01994d56-1200-7000-8000-000000000007");
        var allocations = List.of(
                new OrderItemAllocation(FIRST_CUSTOMER_ID, money("10.0000")),
                new OrderItemAllocation(firstMissingCustomerId, money("5.0000")),
                new OrderItemAllocation(secondMissingCustomerId, money("5.0000")),
                new OrderItemAllocation(firstMissingCustomerId, money("5.0000")));
        when(customerExistence.findExistingIds(any())).thenReturn(Set.of(FIRST_CUSTOMER_ID));

        assertThatThrownBy(() -> service.create(CREDIT_DATE, allocations))
                .isInstanceOfSatisfying(CustomersNotFoundException.class, exception ->
                        assertThat(exception.missingCustomerIds())
                                .containsExactlyElementsOf(new LinkedHashSet<>(
                                        List.of(firstMissingCustomerId, secondMissingCustomerId))));

        verify(customerExistence).findExistingIds(
                new LinkedHashSet<>(List.of(FIRST_CUSTOMER_ID, firstMissingCustomerId, secondMissingCustomerId)));
        verify(saveOrder, never()).save(any());
        verifyNoInteractions(identifierGenerator, clock);
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("CAD"));
    }
}
