package dev.study.orderplatform.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.study.orderplatform.application.CreateOrderService;
import dev.study.orderplatform.application.GetOrderService;
import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;

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
        Order order = Order.create(
                id,
                "customer-123",
                new Money(new BigDecimal("20.5000"), Currency.getInstance("CAD")),
                Instant.parse("2026-09-14T12:00:00Z"),
                LocalDate.parse("2026-09-15"));
        when(createOrderService.create(any())).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "customer-123",
                                  "amount": 20.5000,
                                  "currency": "CAD",
                                  "creditDate": "2026-09-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.amount").value(20.5))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.creditDate").value("2026-09-15"));
    }

    @Test
    void rejectsAnInvalidRequestBeforeCallingTheUseCase() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": "", "amount": 0, "currency": "cad"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.violations").isArray());
    }
}
