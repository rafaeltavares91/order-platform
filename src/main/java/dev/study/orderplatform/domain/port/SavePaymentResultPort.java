package dev.study.orderplatform.domain.port;

import java.util.List;

import dev.study.orderplatform.domain.model.Customer;
import dev.study.orderplatform.domain.model.Order;

public interface SavePaymentResultPort {

    void save(Order order, List<Customer> customers);
}
