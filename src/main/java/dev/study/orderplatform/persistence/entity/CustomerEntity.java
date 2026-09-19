package dev.study.orderplatform.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import dev.study.orderplatform.domain.model.Customer;
import dev.study.orderplatform.domain.model.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_customers_public_id", columnNames = "public_id"),
            @UniqueConstraint(name = "uq_customers_document", columnNames = "document")
        })
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_id_generator")
    @SequenceGenerator(name = "customer_id_generator", sequenceName = "customers_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;

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

    protected CustomerEntity() {
    }

    public static CustomerEntity from(Customer customer) {
        var entity = new CustomerEntity();
        entity.publicId = customer.id();
        entity.document = customer.document();
        entity.name = customer.name();
        entity.balance = customer.balance().amount();
        entity.balanceCurrency = customer.balance().currency().getCurrencyCode();
        entity.createdAt = customer.createdAt();
        entity.updatedAt = customer.updatedAt();
        return entity;
    }

    public Customer toDomain() {
        return Customer.rehydrate(
                publicId,
                document,
                name,
                new Money(balance, Currency.getInstance(balanceCurrency)),
                createdAt,
                updatedAt);
    }

    public Long internalId() {
        return id;
    }

    public UUID publicId() {
        return publicId;
    }
}
