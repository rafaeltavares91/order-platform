package dev.study.orderplatform.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.study.orderplatform.persistence.entity.CustomerEntity;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByDocument(String document);

    List<CustomerEntity> findAllByPublicIdIn(Set<UUID> publicIds);

    @Query("select customer.publicId from CustomerEntity customer where customer.publicId in :publicIds")
    Set<UUID> findExistingIds(@Param("publicIds") Set<UUID> publicIds);
}
