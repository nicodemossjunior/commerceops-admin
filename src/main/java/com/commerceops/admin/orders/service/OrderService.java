package com.commerceops.admin.orders.service;

import com.commerceops.admin.common.error.BusinessRuleException;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.common.security.CurrentUserProvider;
import com.commerceops.admin.orders.dto.OrderDetailResponse;
import com.commerceops.admin.orders.dto.OrderFilter;
import com.commerceops.admin.orders.dto.OrderItemResponse;
import com.commerceops.admin.orders.dto.OrderStatusHistoryResponse;
import com.commerceops.admin.orders.dto.OrderSummaryResponse;
import com.commerceops.admin.orders.model.OrderItem;
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.model.OrderStatusHistory;
import com.commerceops.admin.orders.model.SalesOrder;
import com.commerceops.admin.orders.repository.OrderSpecifications;
import com.commerceops.admin.orders.repository.SalesOrderRepository;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final OrderStatusTransitionService transitionService;
    private final CurrentUserProvider currentUserProvider;

    public OrderService(
            SalesOrderRepository salesOrderRepository,
            OrderStatusTransitionService transitionService,
            CurrentUserProvider currentUserProvider
    ) {
        this.salesOrderRepository = salesOrderRepository;
        this.transitionService = transitionService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> list(OrderFilter filter, Pageable pageable) {
        validateDateRange(filter);
        return PageResponse.from(
                salesOrderRepository.findAll(OrderSpecifications.withFilters(filter), pageable).map(this::toSummary)
        );
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse get(UUID publicId) {
        return toDetail(findActive(publicId));
    }

    @Transactional
    public OrderDetailResponse updateStatus(UUID publicId, OrderStatus status, String reason) {
        SalesOrder order = findActive(publicId);
        transition(order, status, reason);
        salesOrderRepository.flush();
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse cancel(UUID publicId, String reason) {
        return updateStatus(publicId, OrderStatus.CANCELLED, reason);
    }

    @Transactional
    public OrderDetailResponse refund(UUID publicId, String reason) {
        return updateStatus(publicId, OrderStatus.REFUNDED, reason);
    }

    public SalesOrder findActive(UUID publicId) {
        return salesOrderRepository.findByPublicIdAndDeletedFalse(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Order was not found."));
    }

    private void transition(
            SalesOrder order,
            OrderStatus requestedStatus,
            String reason
    ) {
        OrderStatus previousStatus = order.getStatus();
        transitionService.validate(previousStatus, requestedStatus);
        order.changeStatus(requestedStatus);
        order.addStatusHistory(
                previousStatus,
                requestedStatus,
                currentUserProvider.currentUser().id(),
                normalizeReason(reason)
        );
    }

    private String normalizeReason(String reason) {
        return reason == null || reason.isBlank() ? null : reason.trim();
    }

    OrderSummaryResponse toSummary(SalesOrder order) {
        return new OrderSummaryResponse(
                order.getPublicId(),
                order.getOrderNumber(),
                order.getCustomer().getPublicId(),
                order.getCustomer().getName(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getDeliveryStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderDetailResponse toDetail(SalesOrder order) {
        return new OrderDetailResponse(
                order.getPublicId(),
                order.getOrderNumber(),
                order.getCustomer().getPublicId(),
                order.getCustomer().getName(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getDeliveryStatus(),
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getShippingAmount(),
                order.getTotalAmount(),
                order.getCancelledAt(),
                order.getRefundedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(this::toItem).toList(),
                order.getStatusHistory().stream()
                        .sorted(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                        .map(this::toHistory)
                        .toList()
        );
    }

    private OrderItemResponse toItem(OrderItem item) {
        return new OrderItemResponse(
                item.getPublicId(),
                item.getProduct() == null ? null : item.getProduct().getPublicId(),
                item.getProductSku(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getTotalAmount()
        );
    }

    private OrderStatusHistoryResponse toHistory(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getPublicId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getReason(),
                history.getCreatedAt()
        );
    }

    private void validateDateRange(OrderFilter filter) {
        if (filter.createdFrom() != null
                && filter.createdTo() != null
                && filter.createdFrom().isAfter(filter.createdTo())) {
            throw new BusinessRuleException("Created-from date cannot be after created-to date.");
        }
    }
}
