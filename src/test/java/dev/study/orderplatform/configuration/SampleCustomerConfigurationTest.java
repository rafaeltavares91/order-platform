package dev.study.orderplatform.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import dev.study.orderplatform.persistence.entity.CustomerEntity;
import dev.study.orderplatform.persistence.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

class SampleCustomerConfigurationTest {

    private static final Instant NOW = Instant.parse("2026-09-19T12:00:00Z");

    private final CustomerRepository repository = mock(CustomerRepository.class);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final SampleCustomerConfiguration configuration = new SampleCustomerConfiguration();

    @Test
    void createsThreeSampleCustomers() throws Exception {
        when(repository.findExistingIds(anySet())).thenReturn(Set.of());

        configuration.sampleCustomerInitializer(repository, clock).run(new DefaultApplicationArguments());

        var customers = savedCustomers();
        assertThat(customers)
                .extracting(CustomerEntity::toDomain)
                .extracting(customer -> customer.id().toString(), customer -> customer.document(), customer -> customer.name())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                "01994d56-1200-7000-8000-000000000004", "DOC-001", "Ada Lovelace"),
                        org.assertj.core.groups.Tuple.tuple(
                                "01994d56-1200-7000-8000-000000000005", "DOC-002", "Grace Hopper"),
                        org.assertj.core.groups.Tuple.tuple(
                                "01994d56-1200-7000-8000-000000000006", "DOC-003", "Alan Turing"));
        assertThat(customers)
                .extracting(CustomerEntity::toDomain)
                .allSatisfy(customer -> {
                    assertThat(customer.balance().amount()).isZero();
                    assertThat(customer.balance().currency().getCurrencyCode()).isEqualTo("CAD");
                    assertThat(customer.createdAt()).isEqualTo(NOW);
                });
    }

    @Test
    void doesNotInsertCustomersThatAlreadyExist() throws Exception {
        when(repository.findExistingIds(anySet()))
                .thenReturn(Set.of(UUID.fromString("01994d56-1200-7000-8000-000000000004")));

        configuration.sampleCustomerInitializer(repository, clock).run(new DefaultApplicationArguments());

        assertThat(savedCustomers())
                .extracting(CustomerEntity::toDomain)
                .extracting(customer -> customer.id().toString())
                .containsExactly(
                        "01994d56-1200-7000-8000-000000000005",
                        "01994d56-1200-7000-8000-000000000006");
    }

    @SuppressWarnings("unchecked")
    private List<CustomerEntity> savedCustomers() {
        var captor = ArgumentCaptor.forClass(Iterable.class);
        verify(repository).saveAll(captor.capture());
        return (List<CustomerEntity>) captor.getValue();
    }
}
