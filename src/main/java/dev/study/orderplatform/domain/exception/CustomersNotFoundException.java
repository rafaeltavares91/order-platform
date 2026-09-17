package dev.study.orderplatform.domain.exception;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomersNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient Set<UUID> missingCustomerIds;

    public CustomersNotFoundException(Set<UUID> missingCustomerIds) {
        super("Customers not found: " + missingCustomerIds.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(", ")));
        this.missingCustomerIds = Collections.unmodifiableSet(new LinkedHashSet<>(missingCustomerIds));
    }

    public Set<UUID> missingCustomerIds() {
        return missingCustomerIds;
    }
}
