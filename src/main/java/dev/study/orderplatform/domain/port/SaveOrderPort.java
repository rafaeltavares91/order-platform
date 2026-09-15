package dev.study.orderplatform.domain.port;

import dev.study.orderplatform.domain.model.Order;

public interface SaveOrderPort {

    Order save(Order order);
}
