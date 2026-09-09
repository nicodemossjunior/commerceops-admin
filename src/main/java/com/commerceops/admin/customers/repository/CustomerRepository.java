package com.commerceops.admin.customers.repository;

import com.commerceops.admin.customers.model.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByPublicIdAndDeletedFalse(UUID publicId);

    boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

    boolean existsByEmailIgnoreCaseAndDeletedFalseAndIdNot(String email, Long id);
}
