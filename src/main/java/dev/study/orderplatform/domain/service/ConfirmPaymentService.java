package dev.study.orderplatform.domain.service;

import java.time.Clock;
import java.util.HashMap;
import java.util.UUID;

import dev.study.orderplatform.domain.exception.OrderNotFoundException;
import dev.study.orderplatform.domain.exception.PaymentAmountMismatchException;
import dev.study.orderplatform.domain.model.PaymentConfirmation;
import dev.study.orderplatform.domain.model.Customer;
import dev.study.orderplatform.domain.model.OrderStatus;
import dev.study.orderplatform.domain.port.LoadPaymentContextPort;
import dev.study.orderplatform.domain.port.SavePaymentResultPort;

public class ConfirmPaymentService {

    private final LoadPaymentContextPort loadPaymentContext;
    private final SavePaymentResultPort savePaymentResult;
    private final Clock clock;

    public ConfirmPaymentService(
            LoadPaymentContextPort loadPaymentContext,
            SavePaymentResultPort savePaymentResult,
            Clock clock) {
        this.loadPaymentContext = loadPaymentContext;
        this.savePaymentResult = savePaymentResult;
        this.clock = clock;
    }

    public void confirm(PaymentConfirmation confirmation) {
        var context = loadPaymentContext.findByOrderIdForUpdate(confirmation.orderId())
                .orElseThrow(() -> new OrderNotFoundException(confirmation.orderId()));
        var order = context.order();
        if (!order.totalAmount().equals(confirmation.paidAmount())) {
            throw new PaymentAmountMismatchException();
        }
        if (order.status() == OrderStatus.CREDITED) {
            if (confirmation.paymentId().equals(order.paymentId())) {
                return;
            }
            throw new IllegalStateException("order was already credited with another payment");
        }

        var customersById = new HashMap<UUID, Customer>();
        context.customers().forEach(customer -> customersById.put(customer.id(), customer));
        var processedAt = clock.instant();
        for (var item : order.items()) {
            var customer = customersById.get(item.customerId());
            if (customer == null) {
                throw new IllegalStateException("Customer %s no longer exists".formatted(item.customerId()));
            }
            customersById.put(customer.id(), customer.credit(item.amount(), processedAt));
        }
        var creditedOrder = order.credit(confirmation.paymentId(), confirmation.paidAt(), processedAt);
        savePaymentResult.save(creditedOrder, customersById.values().stream().toList());
    }
}
