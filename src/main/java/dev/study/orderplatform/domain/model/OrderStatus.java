package dev.study.orderplatform.domain.model;

public enum OrderStatus {
    CREATED,
    WAITING_PAYMENT,
    PAID,
    CREDITED,
    CANCELED
}
