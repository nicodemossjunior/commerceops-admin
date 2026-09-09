package com.commerceops.admin.customers.repository;

import com.commerceops.admin.customers.model.CustomerNote;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    Page<CustomerNote> findAllByCustomerIdAndDeletedFalse(Long customerId, Pageable pageable);

    Optional<CustomerNote> findByPublicIdAndCustomerIdAndDeletedFalse(UUID publicId, Long customerId);
}
