package com.commerceops.admin.orders.repository;

import com.commerceops.admin.orders.model.SalesOrder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {

    Optional<SalesOrder> findByPublicIdAndDeletedFalse(UUID publicId);

    Page<SalesOrder> findAllByCustomerIdAndDeletedFalse(Long customerId, Pageable pageable);
}
