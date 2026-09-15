package dev.study.orderplatform.order.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.study.orderplatform.order.application.OrderNotFoundException;
import dev.study.orderplatform.order.application.port.in.GetOrderUseCase;
import dev.study.orderplatform.order.application.port.out.LoadOrderPort;
import dev.study.orderplatform.order.domain.model.Order;

@Service
public class GetOrderService implements GetOrderUseCase {

    private final LoadOrderPort loadOrderPort;

    public GetOrderService(LoadOrderPort loadOrderPort) {
        this.loadOrderPort = loadOrderPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getById(UUID id) {
        return loadOrderPort.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
