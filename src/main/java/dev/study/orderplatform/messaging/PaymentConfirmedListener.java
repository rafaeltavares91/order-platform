package dev.study.orderplatform.messaging;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import dev.study.orderplatform.domain.model.PaymentConfirmation;
import dev.study.orderplatform.domain.service.ConfirmPaymentService;
import dev.study.orderplatform.persistence.adapter.InboxMessageStore;
import io.awspring.cloud.sqs.annotation.SqsListener;

@Component
@ConditionalOnProperty(name = "order-platform.messaging.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentConfirmedListener {

    private final InboxMessageStore inbox;
    private final ConfirmPaymentService confirmPaymentService;
    private final Clock clock;

    public PaymentConfirmedListener(
            InboxMessageStore inbox,
            ConfirmPaymentService confirmPaymentService,
            Clock clock) {
        this.inbox = inbox;
        this.confirmPaymentService = confirmPaymentService;
        this.clock = clock;
    }

    @SqsListener("${order-platform.messaging.payment-confirmed-queue}")
    @Transactional
    public void receive(PaymentConfirmedMessage message) {
        var confirmation = message.toDomain();
        if (!inbox.register(confirmation.eventId(), PaymentConfirmation.EVENT_TYPE, clock.instant())) {
            return;
        }
        confirmPaymentService.confirm(confirmation);
    }
}
