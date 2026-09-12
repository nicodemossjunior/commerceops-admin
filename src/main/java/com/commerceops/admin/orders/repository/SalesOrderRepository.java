package com.commerceops.admin.orders.repository;

import com.commerceops.admin.orders.model.SalesOrder;
import com.commerceops.admin.orders.model.OrderStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {

    Optional<SalesOrder> findByPublicIdAndDeletedFalse(UUID publicId);

    Page<SalesOrder> findAllByCustomerIdAndDeletedFalse(Long customerId, Pageable pageable);

    long countByStatusAndDeletedFalse(OrderStatus status);

    @Query("""
            SELECT
                COALESCE(SUM(CASE WHEN o.status <> :cancelledStatus THEN o.totalAmount ELSE 0 END), 0)
                    AS grossRevenue,
                COALESCE(SUM(CASE WHEN o.status NOT IN (:cancelledStatus, :refundedStatus)
                    THEN o.totalAmount ELSE 0 END), 0) AS netRevenue,
                COUNT(o) AS orderCount,
                COALESCE(SUM(CASE WHEN o.status = :cancelledStatus THEN 1 ELSE 0 END), 0)
                    AS cancelledOrderCount,
                COALESCE(SUM(CASE WHEN o.status = :refundedStatus THEN 1 ELSE 0 END), 0)
                    AS refundedOrderCount
            FROM SalesOrder o
            WHERE o.deleted = false
                AND o.createdAt >= :from
                AND o.createdAt <= :to
            """)
    OrderDashboardMetricsProjection summarizeDashboardMetrics(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("cancelledStatus") OrderStatus cancelledStatus,
            @Param("refundedStatus") OrderStatus refundedStatus
    );

    Page<SalesOrder> findByDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(
            Instant from,
            Instant to,
            Pageable pageable
    );
}
