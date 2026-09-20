package dev.study.orderplatform.persistence.adapter;

import java.util.UUID;

public record OutboxMessage(UUID id, String eventType, String payload, int attemptCount) {
}
