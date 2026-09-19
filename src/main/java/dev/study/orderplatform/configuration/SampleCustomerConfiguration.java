package dev.study.orderplatform.configuration;

import java.time.Clock;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import dev.study.orderplatform.domain.model.Customer;
import dev.study.orderplatform.persistence.entity.CustomerEntity;
import dev.study.orderplatform.persistence.repository.CustomerRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile({"local", "dev"})
public class SampleCustomerConfiguration {

    private static final Currency CAD = Currency.getInstance("CAD");

    @Bean
    ApplicationRunner sampleCustomerInitializer(CustomerRepository repository, Clock clock) {
        return arguments -> {
            var now = clock.instant();
            var sampleCustomers = List.of(
                    Customer.create(
                            UUID.fromString("01994d56-1200-7000-8000-000000000004"),
                            "DOC-001",
                            "Ada Lovelace",
                            CAD,
                            now),
                    Customer.create(
                            UUID.fromString("01994d56-1200-7000-8000-000000000005"),
                            "DOC-002",
                            "Grace Hopper",
                            CAD,
                            now),
                    Customer.create(
                            UUID.fromString("01994d56-1200-7000-8000-000000000006"),
                            "DOC-003",
                            "Alan Turing",
                            CAD,
                            now));

            var sampleIds = sampleCustomers.stream().map(Customer::id).collect(Collectors.toSet());
            var existingIds = repository.findExistingIds(sampleIds);

            var missingCustomers = sampleCustomers.stream()
                    .filter(customer -> !existingIds.contains(customer.id()))
                    .map(CustomerEntity::from)
                    .toList();

            repository.saveAll(missingCustomers);
        };
    }
}
