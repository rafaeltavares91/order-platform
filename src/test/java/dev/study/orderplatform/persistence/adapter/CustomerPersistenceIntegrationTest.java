package dev.study.orderplatform.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;

import dev.study.orderplatform.support.PostgreSqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
class CustomerPersistenceIntegrationTest extends PostgreSqlIntegrationTest {

    private static final String FIRST_CUSTOMER_ID = "01994d56-1200-7000-8000-000000000004";
    private static final String SECOND_CUSTOMER_ID = "01994d56-1200-7000-8000-000000000005";
    private static final String MISSING_CUSTOMER_ID = "01994d56-1200-7000-8000-000000000099";

    @Autowired
    private CustomerPersistenceAdapter adapter;

    @Test
    void findsOnlyExistingCustomerIds() {
        insertCustomer(FIRST_CUSTOMER_ID, "DOC-001", "Ada Lovelace");
        insertCustomer(SECOND_CUSTOMER_ID, "DOC-002", "Grace Hopper");

        var existingIds = adapter.findExistingIds(Set.of(
                UUID.fromString(FIRST_CUSTOMER_ID),
                UUID.fromString(SECOND_CUSTOMER_ID),
                UUID.fromString(MISSING_CUSTOMER_ID)));

        assertThat(existingIds).containsExactlyInAnyOrder(
                UUID.fromString(FIRST_CUSTOMER_ID), UUID.fromString(SECOND_CUSTOMER_ID));
    }

    @Test
    void enforcesUniqueCustomerDocuments() {
        insertCustomer(FIRST_CUSTOMER_ID, "DOC-001", "Ada Lovelace");

        assertThatThrownBy(() -> insertCustomer(SECOND_CUSTOMER_ID, "DOC-001", "Grace Hopper"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
