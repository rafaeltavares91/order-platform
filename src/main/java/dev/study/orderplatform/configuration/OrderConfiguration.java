package dev.study.orderplatform.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.study.orderplatform.order.domain.CreateOrderService;
import dev.study.orderplatform.order.domain.GetOrderService;
import dev.study.orderplatform.order.domain.LoadOrderPort;
import dev.study.orderplatform.order.domain.OrderIdGenerator;
import dev.study.orderplatform.order.domain.SaveOrderPort;

@Configuration(proxyBeanMethods = false)
public class OrderConfiguration {

    @Bean
    CreateOrderService createOrderService(
            SaveOrderPort saveOrder, OrderIdGenerator orderIdGenerator, Clock clock) {
        return new CreateOrderService(saveOrder, orderIdGenerator, clock);
    }

    @Bean
    GetOrderService getOrderService(LoadOrderPort loadOrder) {
        return new GetOrderService(loadOrder);
    }
}
