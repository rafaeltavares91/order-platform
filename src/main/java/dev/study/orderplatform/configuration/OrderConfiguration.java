package dev.study.orderplatform.configuration;

import java.time.Clock;

import dev.study.orderplatform.domain.service.CreateOrderService;
import dev.study.orderplatform.domain.service.GetOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.study.orderplatform.domain.port.LoadOrderPort;
import dev.study.orderplatform.domain.port.OrderIdGenerator;
import dev.study.orderplatform.domain.port.SaveOrderPort;

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
