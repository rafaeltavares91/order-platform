package dev.study.orderplatform.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import dev.study.orderplatform.persistence.adapter.OutboxMessage;
import dev.study.orderplatform.persistence.adapter.OutboxMessageStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    private static final Instant NOW = Instant.parse("2026-09-19T12:00:00Z");
    private static final UUID EVENT_ID = UUID.fromString("01994d56-1200-7000-8000-000000000008");

    @Mock
    private OutboxMessageStore store;

    @Mock
    private SnsClient snsClient;

    @Test
    void publishesWithStableEventAttributesAndMarksTheMessage() {
        var message = new OutboxMessage(EVENT_ID, "PaymentRequested.v1", "{\"orderId\":\"order-1\"}", 1);
        when(store.claim(20, NOW, Duration.ofSeconds(30))).thenReturn(List.of(message));
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().build());
        var publisher = new OutboxPublisher(
                store,
                snsClient,
                Clock.fixed(NOW, ZoneOffset.UTC),
                "arn:aws:sns:us-east-1:000000000000:order-events",
                20);

        publisher.publishPending();

        var request = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(request.capture());
        assertThat(request.getValue().message()).isEqualTo(message.payload());
        assertThat(request.getValue().messageAttributes().get("eventType").stringValue())
                .isEqualTo("PaymentRequested.v1");
        assertThat(request.getValue().messageAttributes().get("eventId").stringValue())
                .isEqualTo(EVENT_ID.toString());
        verify(store).markPublished(EVENT_ID, NOW);
    }
}
