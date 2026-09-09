package com.commerceops.admin.orders;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.orders.dto.OrderDetailResponse;
import com.commerceops.admin.orders.dto.OrderCancelRequest;
import com.commerceops.admin.orders.dto.OrderFilter;
import com.commerceops.admin.orders.dto.OrderRefundRequest;
import com.commerceops.admin.orders.dto.OrderStatusUpdateRequest;
import com.commerceops.admin.orders.dto.OrderSummaryResponse;
import com.commerceops.admin.orders.model.DeliveryStatus;
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.model.PaymentStatus;
import com.commerceops.admin.orders.service.OrderService;
import java.time.Instant;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final String READ_ROLES = "hasAnyRole('ADMIN', 'MANAGER', 'SUPPORT', 'READ_ONLY', 'CATALOG')";
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public PageResponse<OrderSummaryResponse> list(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) DeliveryStatus deliveryStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            Pageable pageable
    ) {
        return orderService.list(
                new OrderFilter(
                        orderNumber,
                        customerId,
                        status,
                        paymentStatus,
                        deliveryStatus,
                        createdFrom,
                        createdTo
                ),
                pageable
        );
    }

    @GetMapping("/{publicId}")
    @PreAuthorize(READ_ROLES)
    public OrderDetailResponse get(@PathVariable UUID publicId) {
        return orderService.get(publicId);
    }

    @PatchMapping("/{publicId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public OrderDetailResponse updateStatus(
            @PathVariable UUID publicId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return orderService.updateStatus(publicId, request.status(), request.reason());
    }

    @PostMapping("/{publicId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public OrderDetailResponse cancel(
            @PathVariable UUID publicId,
            @Valid @RequestBody OrderCancelRequest request
    ) {
        return orderService.cancel(publicId, request.reason());
    }

    @PostMapping("/{publicId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public OrderDetailResponse refund(
            @PathVariable UUID publicId,
            @Valid @RequestBody OrderRefundRequest request
    ) {
        return orderService.refund(publicId, request.reason());
    }
}
