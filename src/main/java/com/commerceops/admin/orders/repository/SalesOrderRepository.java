package com.commerceops.admin.orders.repository;

import com.commerceops.admin.orders.model.SalesOrder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {

    Optional<SalesOrder> findByPublicIdAndDeletedFalse(UUID publicId);
}
