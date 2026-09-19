package dev.study.orderplatform.persistence.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.study.orderplatform.persistence.entity.CustomerEntity;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity> findByDocument(String document);

    @Query("select customer.id from CustomerEntity customer where customer.id in :customerIds")
    Set<UUID> findExistingIds(@Param("customerIds") Set<UUID> customerIds);
}
