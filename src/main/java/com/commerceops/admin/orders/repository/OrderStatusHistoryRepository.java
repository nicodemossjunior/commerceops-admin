package com.commerceops.admin.orders.repository;

import com.commerceops.admin.orders.model.OrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findAllBySalesOrderIdOrderByCreatedAtAsc(Long salesOrderId);
}
