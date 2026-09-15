package dev.study.orderplatform.order.domain;

import dev.study.orderplatform.order.domain.model.Order;

public interface SaveOrderPort {

    Order save(Order order);
}
