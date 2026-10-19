package dev.study.orderplatform.domain.exception;

public class PaymentAmountMismatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PaymentAmountMismatchException() {
        super("Payment amount and currency must match the order total");
    }
}
