package dev.study.orderplatform.domain;

import java.util.UUID;

public class GetOrderService {

    private final LoadOrderPort loadOrder;

    public GetOrderService(LoadOrderPort loadOrder) {
        this.loadOrder = loadOrder;
    }

    public Order getById(UUID id) {
        return loadOrder.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
