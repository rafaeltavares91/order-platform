package dev.study.orderplatform.application;

import java.util.UUID;

import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.port.LoadOrderPort;

public class GetOrderService {

    private final LoadOrderPort loadOrder;

    public GetOrderService(LoadOrderPort loadOrder) {
        this.loadOrder = loadOrder;
    }

    public Order getById(UUID id) {
        return loadOrder.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
