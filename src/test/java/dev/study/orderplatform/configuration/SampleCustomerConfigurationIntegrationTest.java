package dev.study.orderplatform.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import dev.study.orderplatform.support.PostgreSqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class SampleCustomerConfigurationIntegrationTest extends PostgreSqlIntegrationTest {

    @Autowired
    @Qualifier("sampleCustomerInitializer")
    private ApplicationRunner initializer;

    @Test
    void createsSampleCustomersWithInternalSequenceIdsAndDoesNotDuplicateThem() throws Exception {
        initializer.run(new DefaultApplicationArguments());
        initializer.run(new DefaultApplicationArguments());

        assertThat(jdbcTemplate.queryForList(
                        "SELECT public_id::text FROM customers ORDER BY document", String.class))
                .containsExactly(
                        "01994d56-1200-7000-8000-000000000004",
                        "01994d56-1200-7000-8000-000000000005",
                        "01994d56-1200-7000-8000-000000000006");
        assertThat(jdbcTemplate.queryForList("SELECT id FROM customers", Long.class))
                .hasSize(3)
                .allSatisfy(id -> assertThat(id).isPositive());
    }
}
