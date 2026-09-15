package dev.study.orderplatform.order.domain;

import java.util.UUID;

import dev.study.orderplatform.order.domain.model.Order;

public class GetOrderService {

    private final LoadOrderPort loadOrder;

    public GetOrderService(LoadOrderPort loadOrder) {
        this.loadOrder = loadOrder;
    }

    public Order getById(UUID id) {
        return loadOrder.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
