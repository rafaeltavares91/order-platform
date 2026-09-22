package dev.study.orderplatform.configuration;

import java.time.Clock;

import dev.study.orderplatform.domain.service.CreateOrderService;
import dev.study.orderplatform.domain.service.GetOrderService;
import dev.study.orderplatform.domain.service.ConfirmPaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.study.orderplatform.domain.port.CustomerExistencePort;
import dev.study.orderplatform.domain.port.IdentifierGenerator;
import dev.study.orderplatform.domain.port.LoadOrderPort;
import dev.study.orderplatform.domain.port.SaveOrderPort;
import dev.study.orderplatform.domain.port.LoadPaymentContextPort;
import dev.study.orderplatform.domain.port.SavePaymentResultPort;

@Configuration(proxyBeanMethods = false)
public class OrderConfiguration {

    @Bean
    CreateOrderService createOrderService(
            SaveOrderPort saveOrder,
            CustomerExistencePort customerExistence,
            IdentifierGenerator identifierGenerator,
            Clock clock) {
        return new CreateOrderService(saveOrder, customerExistence, identifierGenerator, clock);
    }

    @Bean
    GetOrderService getOrderService(LoadOrderPort loadOrder) {
        return new GetOrderService(loadOrder);
    }

    @Bean
    ConfirmPaymentService confirmPaymentService(
            LoadPaymentContextPort loadPaymentContext,
            SavePaymentResultPort savePaymentResult,
            Clock clock) {
        return new ConfirmPaymentService(loadPaymentContext, savePaymentResult, clock);
    }

}
