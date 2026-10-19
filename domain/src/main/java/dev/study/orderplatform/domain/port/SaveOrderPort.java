package dev.study.orderplatform.domain.port;

import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.PaymentRequested;

public interface SaveOrderPort {

    Order save(Order order, PaymentRequested paymentRequested);
}
