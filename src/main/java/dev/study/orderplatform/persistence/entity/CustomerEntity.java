package dev.study.orderplatform.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Customer;
import dev.study.orderplatform.domain.model.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = @UniqueConstraint(name = "uq_customers_document", columnNames = "document"))
public class CustomerEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String document;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = Money.PRECISION, scale = Money.SCALE)
    private BigDecimal balance;

    @Column(name = "balance_currency", nullable = false, length = 3)
    private String balanceCurrency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected CustomerEntity() {
    }

    public static CustomerEntity from(Customer customer) {
        var entity = new CustomerEntity();
        entity.id = customer.id();
        entity.document = customer.document();
        entity.name = customer.name();
        entity.balance = customer.balance().amount();
        entity.balanceCurrency = customer.balance().currency().getCurrencyCode();
        entity.createdAt = customer.createdAt();
        entity.updatedAt = customer.updatedAt();
        entity.version = customer.version();
        return entity;
    }

    public Customer toDomain() {
        return Customer.rehydrate(
                id,
                document,
                name,
                new Money(balance, Currency.getInstance(balanceCurrency)),
                createdAt,
                updatedAt,
                version);
    }
}
