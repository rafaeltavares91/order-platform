package dev.study.orderplatform.order.application.port.out;

import dev.study.orderplatform.order.domain.model.Order;

public interface SaveOrderPort {

    Order save(Order order);
}
