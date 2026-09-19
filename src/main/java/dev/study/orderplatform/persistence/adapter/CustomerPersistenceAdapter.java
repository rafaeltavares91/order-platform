package dev.study.orderplatform.persistence.adapter;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.study.orderplatform.domain.port.CustomerExistencePort;
import dev.study.orderplatform.persistence.repository.CustomerRepository;

@Repository
public class CustomerPersistenceAdapter implements CustomerExistencePort {

    private final CustomerRepository repository;

    public CustomerPersistenceAdapter(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> findExistingIds(Set<UUID> customerIds) {
        if (customerIds.isEmpty()) {
            return Set.of();
        }
        return repository.findExistingIds(customerIds);
    }
}
