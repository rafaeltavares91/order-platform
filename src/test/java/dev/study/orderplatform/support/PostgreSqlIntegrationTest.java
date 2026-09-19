package dev.study.orderplatform.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class PostgreSqlIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer POSTGRES = startPostgres();

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE order_items, orders, customers CASCADE");
    }

    protected void insertCustomer(String id, String document, String name) {
        jdbcTemplate.update(
                """
                INSERT INTO customers (
                    public_id, document, name, balance, balance_currency, created_at, updated_at
                ) VALUES (?::uuid, ?, ?, 0.0000, 'CAD', '2026-09-14T12:00:00Z', '2026-09-14T12:00:00Z')
                """,
                id,
                document,
                name);
    }

    private static PostgreSQLContainer startPostgres() {
        var postgres = new PostgreSQLContainer("postgres:18.6-alpine3.23");
        postgres.start();
        return postgres;
    }
}
