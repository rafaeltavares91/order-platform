package dev.study.orderplatform.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@EnabledIfEnvironmentVariable(named = "LOCALSTACK_AUTH_TOKEN", matches = ".+")
class MessagingTopologyIntegrationTest {

    private static final LocalStackContainer LOCALSTACK = new LocalStackContainer(
                    DockerImageName.parse("localstack/localstack:latest"))
            .withServices("sns", "sqs")
            .withEnv("LOCALSTACK_AUTH_TOKEN", System.getenv().getOrDefault("LOCALSTACK_AUTH_TOKEN", "missing"));

    private static SnsClient sns;
    private static SqsClient sqs;

    @BeforeAll
    static void startLocalStack() {
        LOCALSTACK.start();
        var credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(
                LOCALSTACK.getAccessKey(), LOCALSTACK.getSecretKey()));
        var region = Region.of(LOCALSTACK.getRegion());
        sns = SnsClient.builder()
                .endpointOverride(LOCALSTACK.getEndpoint())
                .credentialsProvider(credentials)
                .region(region)
                .build();
        sqs = SqsClient.builder()
                .endpointOverride(LOCALSTACK.getEndpoint())
                .credentialsProvider(credentials)
                .region(region)
                .build();
    }

    @AfterAll
    static void stopLocalStack() {
        if (sns != null) {
            sns.close();
        }
        if (sqs != null) {
            sqs.close();
        }
        LOCALSTACK.stop();
    }

    @Test
    void fansOutOnlyMatchingEventsToEverySubscribedQueue() {
        var topicArn = sns.createTopic(request -> request.name("order-events")).topicArn();
        var firstQueueUrl = createQueue("payment-requested-first");
        var secondQueueUrl = createQueue("payment-requested-second");
        subscribe(topicArn, queueArn(firstQueueUrl));
        subscribe(topicArn, queueArn(secondQueueUrl));

        publish(topicArn, "PaymentRequested.v1", "{\"eventId\":\"event-1\"}");

        assertThat(receiveEventually(firstQueueUrl)).isEqualTo("{\"eventId\":\"event-1\"}");
        assertThat(receiveEventually(secondQueueUrl)).isEqualTo("{\"eventId\":\"event-1\"}");

        publish(topicArn, "OtherEvent.v1", "{\"eventId\":\"event-2\"}");
        assertThat(receive(firstQueueUrl)).isNull();
        assertThat(receive(secondQueueUrl)).isNull();
    }

    private static String createQueue(String name) {
        return sqs.createQueue(CreateQueueRequest.builder().queueName(name).build()).queueUrl();
    }

    private static String queueArn(String queueUrl) {
        return sqs.getQueueAttributes(GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(QueueAttributeName.QUEUE_ARN)
                        .build())
                .attributes()
                .get(QueueAttributeName.QUEUE_ARN);
    }

    private static void subscribe(String topicArn, String queueArn) {
        var subscriptionArn = sns.subscribe(SubscribeRequest.builder()
                        .topicArn(topicArn)
                        .protocol("sqs")
                        .endpoint(queueArn)
                        .attributes(Map.of("RawMessageDelivery", "true"))
                        .build())
                .subscriptionArn();
        sns.setSubscriptionAttributes(SetSubscriptionAttributesRequest.builder()
                .subscriptionArn(subscriptionArn)
                .attributeName("FilterPolicy")
                .attributeValue("{\"eventType\":[\"PaymentRequested.v1\"]}")
                .build());
    }

    private static void publish(String topicArn, String eventType, String body) {
        sns.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .message(body)
                .messageAttributes(Map.of(
                        "eventType",
                        MessageAttributeValue.builder().dataType("String").stringValue(eventType).build()))
                .build());
    }

    private static String receive(String queueUrl) {
        var messages = sqs.receiveMessage(ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .waitTimeSeconds((int) Duration.ofSeconds(1).toSeconds())
                        .maxNumberOfMessages(1)
                        .build())
                .messages();
        return messages.isEmpty() ? null : messages.getFirst().body();
    }

    private static String receiveEventually(String queueUrl) {
        for (var attempt = 0; attempt < 5; attempt++) {
            var message = receive(queueUrl);
            if (message != null) {
                return message;
            }
        }
        return null;
    }
}
