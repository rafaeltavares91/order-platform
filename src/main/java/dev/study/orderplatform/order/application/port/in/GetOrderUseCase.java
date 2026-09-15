package dev.study.orderplatform.order.application.port.in;

import java.util.UUID;

import dev.study.orderplatform.order.domain.model.Order;

public interface GetOrderUseCase {

    Order getById(UUID id);
}
