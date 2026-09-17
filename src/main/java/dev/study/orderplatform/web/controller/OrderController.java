package dev.study.orderplatform.web.controller;

import java.net.URI;
import java.util.UUID;

import dev.study.orderplatform.domain.service.CreateOrderService;
import dev.study.orderplatform.domain.service.GetOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.study.orderplatform.web.dto.CreateOrderRequest;
import dev.study.orderplatform.web.dto.OrderResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderService createOrderService;
    private final GetOrderService getOrderService;

    public OrderController(CreateOrderService createOrderService, GetOrderService getOrderService) {
        this.createOrderService = createOrderService;
        this.getOrderService = getOrderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        var order = createOrderService.create(request.toCommand());
        return ResponseEntity.created(URI.create("/orders/" + order.id())).body(OrderResponse.from(order));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getById(@PathVariable UUID orderId) {
        return OrderResponse.from(getOrderService.getById(orderId));
    }
}
