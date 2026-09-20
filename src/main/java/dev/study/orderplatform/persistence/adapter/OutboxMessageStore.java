package dev.study.orderplatform.persistence.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class OutboxMessageStore {

    private final JdbcTemplate jdbcTemplate;

    public OutboxMessageStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public List<OutboxMessage> claim(int batchSize, Instant now, Duration lease) {
        var messages = jdbcTemplate.query(
                """
                SELECT public_id, event_type, payload::text, attempt_count
                FROM outbox_messages
                WHERE (status = 'PENDING'
                   OR (status = 'PROCESSING' AND locked_until < ?))
                  AND next_attempt_at <= ?
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED
                LIMIT ?
                """,
                OutboxMessageStore::map,
                timestamp(now),
                timestamp(now),
                batchSize);
        for (var message : messages) {
            jdbcTemplate.update(
                    """
                    UPDATE outbox_messages
                    SET status = 'PROCESSING', attempt_count = attempt_count + 1, locked_until = ?, last_error = NULL
                    WHERE public_id = ?
                    """,
                    timestamp(now.plus(lease)),
                    message.id());
        }
        return messages;
    }

    @Transactional
    public void markPublished(UUID id, Instant publishedAt) {
        jdbcTemplate.update(
                """
                UPDATE outbox_messages
                SET status = 'PUBLISHED', published_at = ?, locked_until = NULL
                WHERE public_id = ?
                """,
                timestamp(publishedAt),
                id);
    }

    @Transactional
    public void releaseForRetry(UUID id, Instant nextAttemptAt, String error) {
        jdbcTemplate.update(
                """
                UPDATE outbox_messages
                SET status = 'PENDING', next_attempt_at = ?, locked_until = NULL, last_error = ?
                WHERE public_id = ?
                """,
                timestamp(nextAttemptAt),
                error.substring(0, Math.min(error.length(), 1000)),
                id);
    }

    private static OutboxMessage map(ResultSet resultSet, int rowNumber) throws SQLException {
        return new OutboxMessage(
                resultSet.getObject("public_id", UUID.class),
                resultSet.getString("event_type"),
                resultSet.getString("payload"),
                resultSet.getInt("attempt_count") + 1);
    }

    private static OffsetDateTime timestamp(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
