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
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.study.orderplatform.domain.model.Money;
import dev.study.orderplatform.domain.model.Order;
import dev.study.orderplatform.domain.model.OrderItem;
import dev.study.orderplatform.domain.service.CreateOrderService;
import dev.study.orderplatform.domain.service.GetOrderService;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private static final UUID ORDER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000001");
    private static final UUID FIRST_CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000004");
    private static final UUID SECOND_CUSTOMER_ID = UUID.fromString("01994d56-1200-7000-8000-000000000005");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateOrderService createOrderService;

    @MockitoBean
    private GetOrderService getOrderService;

    @Test
    void createsAnOrderWithMultipleItemsAndReturnsTheCalculatedTotal() throws Exception {
        var order = order();
        when(createOrderService.create(any())).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/" + ORDER_ID))
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.totalAmount").value(25.0))
                .andExpect(jsonPath("$.currency").value("CAD"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.creditDate").value("2026-09-15"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].customerId").value(FIRST_CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.items[0].status").value("PENDING"))
                .andExpect(jsonPath("$.items[1].customerId").value(SECOND_CUSTOMER_ID.toString()));
    }

    @Test
    void rejectsAnOrderWithoutItems() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"creditDate": "2026-09-15", "items": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void rejectsAnExternallySuppliedTotalAmount() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "creditDate": "2026-09-15",
                                  "totalAmount": 1.0000,
                                  "items": [{
                                    "customerId": "01994d56-1200-7000-8000-000000000004",
                                    "amount": 20.5000,
                                    "currency": "CAD"
                                  }]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    private static String validRequest() {
        return """
                {
                  "creditDate": "2026-09-15",
                  "items": [
                    {
                      "customerId": "01994d56-1200-7000-8000-000000000004",
                      "amount": 20.5000,
                      "currency": "CAD"
                    },
                    {
                      "customerId": "01994d56-1200-7000-8000-000000000005",
                      "amount": 4.5000,
                      "currency": "CAD"
                    }
                  ]
                }
                """;
    }

    private static Order order() {
        var now = Instant.parse("2026-09-14T12:00:00Z");
        var firstItem = OrderItem.create(
                UUID.fromString("01994d56-1200-7000-8000-000000000002"),
                ORDER_ID,
                FIRST_CUSTOMER_ID,
                money("20.5000"),
                now);
        var secondItem = OrderItem.create(
                UUID.fromString("01994d56-1200-7000-8000-000000000003"),
                ORDER_ID,
                SECOND_CUSTOMER_ID,
                money("4.5000"),
                now);
        return Order.create(
                ORDER_ID, LocalDate.parse("2026-09-15"), List.of(firstItem, secondItem), now);
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("CAD"));
    }
}
