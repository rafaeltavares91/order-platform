package dev.study.orderplatform.order.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.study.orderplatform.order.domain.CreateOrderService;
import dev.study.orderplatform.order.domain.GetOrderService;
import dev.study.orderplatform.order.domain.model.Money;
import dev.study.orderplatform.order.domain.model.Order;
import dev.study.orderplatform.order.domain.model.OrderLine;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateOrderService createOrderService;

    @MockitoBean
    private GetOrderService getOrderService;

    @Test
    void createsAnOrderAtTheUnversionedResourceUrl() throws Exception {
        UUID id = UUID.fromString("01994d56-1200-7000-8000-000000000001");
        Currency currency = Currency.getInstance("CAD");
        Order order = Order.place(
                id,
                "customer-123",
                currency,
                List.of(new OrderLine("SKU-1", 2, new Money(new BigDecimal("10.2500"), currency))),
                Instant.parse("2026-09-14T12:00:00Z"));
        when(createOrderService.create(any())).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "customer-123",
                                  "currency": "CAD",
                                  "lines": [{"sku": "SKU-1", "quantity": 2, "unitPrice": 10.2500}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.total").value(20.5));
    }

    @Test
    void rejectsAnInvalidRequestBeforeCallingTheUseCase() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": "", "currency": "cad", "lines": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.violations").isArray());
    }
}
