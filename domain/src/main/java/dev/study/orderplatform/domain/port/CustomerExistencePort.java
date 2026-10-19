package dev.study.orderplatform.domain.port;

import java.util.Set;
import java.util.UUID;

public interface CustomerExistencePort {

    Set<UUID> findExistingIds(Set<UUID> customerIds);
}
