package dev.study.orderplatform.domain.model;

import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public final class Customer {

    private final UUID id;
    private final String document;
    private final String name;
    private final Money balance;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Customer(
            UUID id,
            String document,
            String name,
            Money balance,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.document = requireText(document, "document", 50);
        this.name = requireText(name, "name", 200);
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt must not be before createdAt");
        }
    }

    public static Customer create(UUID id, String document, String name, Currency currency, Instant createdAt) {
        return new Customer(id, document, name, Money.zero(currency), createdAt, createdAt);
    }

    public static Customer rehydrate(
            UUID id,
            String document,
            String name,
            Money balance,
            Instant createdAt,
            Instant updatedAt) {
        return new Customer(id, document, name, balance, createdAt, updatedAt);
    }

    public Customer credit(Money amount, Instant creditedAt) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(creditedAt, "creditedAt must not be null");
        return new Customer(id, document, name, balance.add(amount), createdAt, creditedAt);
    }

    private static String requireText(String value, String field, int maximumLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        if (value.length() > maximumLength) {
            throw new IllegalArgumentException(field + " must not exceed " + maximumLength + " characters");
        }
        return value;
    }

    public UUID id() {
        return id;
    }

    public String document() {
        return document;
    }

    public String name() {
        return name;
    }

    public Money balance() {
        return balance;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

}
