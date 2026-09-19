package dev.study.orderplatform.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import dev.study.orderplatform.support.PostgreSqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest extends PostgreSqlIntegrationTest {

    private static final String FIRST_CUSTOMER_ID = "01994d56-1200-7000-8000-000000000004";
    private static final String SECOND_CUSTOMER_ID = "01994d56-1200-7000-8000-000000000005";
    private static final String MISSING_CUSTOMER_ID = "01994d56-1200-7000-8000-000000000099";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsPersistsAndRetrievesAnOrderThroughHttp() throws Exception {
        insertCustomer(FIRST_CUSTOMER_ID, "DOC-001", "Ada Lovelace");
        insertCustomer(SECOND_CUSTOMER_ID, "DOC-002", "Grace Hopper");

        var createResult = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.creditDate").value("2099-09-15"))
                .andExpect(jsonPath("$.totalAmount").value(25.0))
                .andExpect(jsonPath("$.currency").value("CAD"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].customerId").value(FIRST_CUSTOMER_ID))
                .andExpect(jsonPath("$.items[0].amount").value(20.5))
                .andExpect(jsonPath("$.items[0].status").value("PENDING"))
                .andExpect(jsonPath("$.items[1].customerId").value(SECOND_CUSTOMER_ID))
                .andExpect(jsonPath("$.items[1].amount").value(4.5))
                .andReturn();

        var location = createResult.getResponse().getHeader("Location");
        assertThat(location).isNotBlank();

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM orders", Long.class)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("SELECT total_amount FROM orders", BigDecimal.class))
                .isEqualByComparingTo("25.0000");
        assertThat(jdbcTemplate.queryForList(
                        "SELECT customer_id, amount FROM order_items ORDER BY item_index"))
                .satisfiesExactly(
                        row -> assertItem(row, FIRST_CUSTOMER_ID, "20.5000"),
                        row -> assertItem(row, SECOND_CUSTOMER_ID, "4.5000"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(25.0))
                .andExpect(jsonPath("$.items[0].customerId").value(FIRST_CUSTOMER_ID))
                .andExpect(jsonPath("$.items[1].customerId").value(SECOND_CUSTOMER_ID));
    }

    @Test
    void rejectsInvalidRequestDataWithoutPersistingAnOrder() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"creditDate": "2099-09-15", "items": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.violations[0].field").value("items"));

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM orders", Long.class)).isZero();
    }

    @Test
    void rejectsUnknownJsonProperties() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "creditDate": "2099-09-15",
                                  "totalAmount": 1.0000,
                                  "items": [{
                                    "customerId": "%s",
                                    "amount": 20.5000,
                                    "currency": "CAD"
                                  }]
                                }
                                """.formatted(FIRST_CUSTOMER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void reportsEveryMissingCustomerWithoutPersistingAnOrder() throws Exception {
        insertCustomer(FIRST_CUSTOMER_ID, "DOC-001", "Ada Lovelace");

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "creditDate": "2099-09-15",
                                  "items": [
                                    {"customerId": "%s", "amount": 20.5000, "currency": "CAD"},
                                    {"customerId": "%s", "amount": 4.5000, "currency": "CAD"}
                                  ]
                                }
                                """.formatted(FIRST_CUSTOMER_ID, MISSING_CUSTOMER_ID)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Customers not found"))
                .andExpect(jsonPath("$.detail").value(
                        "One or more order items reference customers that do not exist"))
                .andExpect(jsonPath("$.missingCustomerIds[0]").value(MISSING_CUSTOMER_ID));

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM orders", Long.class)).isZero();
    }

    @Test
    void returnsAProblemResponseWhenTheOrderDoesNotExist() throws Exception {
        var orderId = UUID.fromString("01994d56-1200-7000-8000-000000000001");

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"))
                .andExpect(jsonPath("$.detail").value("Order %s was not found".formatted(orderId)));
    }

    private static void assertItem(Map<String, Object> row, String customerId, String amount) {
        assertThat(row.get("customer_id")).hasToString(customerId);
        assertThat((BigDecimal) row.get("amount")).isEqualByComparingTo(amount);
    }

    private static String validRequest() {
        return """
                {
                  "creditDate": "2099-09-15",
                  "items": [
                    {"customerId": "%s", "amount": 20.5000, "currency": "CAD"},
                    {"customerId": "%s", "amount": 4.5000, "currency": "CAD"}
                  ]
                }
                """.formatted(FIRST_CUSTOMER_ID, SECOND_CUSTOMER_ID);
    }
}
