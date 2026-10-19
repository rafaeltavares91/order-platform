package dev.study.orderplatform.domain.port;

import java.util.Optional;
import java.util.UUID;

import dev.study.orderplatform.domain.model.PaymentContext;

public interface LoadPaymentContextPort {

    Optional<PaymentContext> findByOrderIdForUpdate(UUID orderId);
}
