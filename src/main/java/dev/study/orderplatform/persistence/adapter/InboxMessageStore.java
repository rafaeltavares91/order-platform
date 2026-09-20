package dev.study.orderplatform.persistence.adapter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InboxMessageStore {

    private final JdbcTemplate jdbcTemplate;

    public InboxMessageStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean register(UUID eventId, String eventType, Instant receivedAt) {
        return jdbcTemplate.update(
                        """
                        INSERT INTO inbox_messages (event_id, event_type, received_at)
                        VALUES (?, ?, ?)
                        ON CONFLICT (event_id) DO NOTHING
                        """,
                        eventId,
                        eventType,
                        OffsetDateTime.ofInstant(receivedAt, ZoneOffset.UTC))
                == 1;
    }
}
