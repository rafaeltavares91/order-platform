package dev.study.orderplatform.order.application.port.in;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import dev.study.orderplatform.order.domain.model.Order;

public interface CreateOrderUseCase {

    Order create(CreateOrderCommand command);

    record CreateOrderCommand(String customerId, Currency currency, List<Line> lines) {

        public CreateOrderCommand {
            lines = List.copyOf(lines);
        }
    }

    record Line(String sku, int quantity, BigDecimal unitPrice) {
    }
}
