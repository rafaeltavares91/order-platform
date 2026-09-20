package dev.study.orderplatform.messaging;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import dev.study.orderplatform.persistence.adapter.OutboxMessageStore;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
@ConditionalOnProperty(name = "order-platform.messaging.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private final OutboxMessageStore store;
    private final SnsClient snsClient;
    private final Clock clock;
    private final String topicArn;
    private final int batchSize;

    public OutboxPublisher(
            OutboxMessageStore store,
            SnsClient snsClient,
            Clock clock,
            @Value("${order-platform.messaging.order-events-topic-arn}") String topicArn,
            @Value("${order-platform.messaging.outbox.batch-size:20}") int batchSize) {
        this.store = store;
        this.snsClient = snsClient;
        this.clock = clock;
        this.topicArn = topicArn;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${order-platform.messaging.outbox.poll-delay:1000}")
    public void publishPending() {
        var messages = store.claim(batchSize, clock.instant(), Duration.ofSeconds(30));
        for (var message : messages) {
            try {
                snsClient.publish(PublishRequest.builder()
                        .topicArn(topicArn)
                        .message(message.payload())
                        .messageAttributes(java.util.Map.of(
                                "eventType",
                                MessageAttributeValue.builder()
                                        .dataType("String")
                                        .stringValue(message.eventType())
                                        .build(),
                                "eventId",
                                MessageAttributeValue.builder()
                                        .dataType("String")
                                        .stringValue(message.id().toString())
                                        .build()))
                        .build());
                store.markPublished(message.id(), clock.instant());
            } catch (RuntimeException exception) {
                var delaySeconds = Math.min(300, 1L << Math.min(message.attemptCount(), 8));
                store.releaseForRetry(
                        message.id(),
                        clock.instant().plusSeconds(delaySeconds),
                        exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            }
        }
    }
}
