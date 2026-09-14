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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Orders", description = "Order lookup, fulfillment status, cancellation, and refund operations")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private static final String READ_ROLES = "hasAnyRole('ADMIN', 'MANAGER', 'SUPPORT', 'READ_ONLY', 'CATALOG')";
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    @Operation(summary = "List and filter orders", description = "Returns non-deleted orders with pagination.")
    public PageResponse<OrderSummaryResponse> list(
            @Parameter(description = "Case-insensitive partial order number")
            @RequestParam(required = false) String orderNumber,
            @Parameter(description = "Customer public UUID")
            @RequestParam(required = false) UUID customerId,
            @Parameter(description = "Order lifecycle status")
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Payment summary status")
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @Parameter(description = "Delivery summary status")
            @RequestParam(required = false) DeliveryStatus deliveryStatus,
            @Parameter(description = "Inclusive order creation timestamp lower bound")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @Parameter(description = "Inclusive order creation timestamp upper bound")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @ParameterObject Pageable pageable
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
    @Operation(summary = "Get an order by public ID", description = "Includes customer, item snapshots, totals, payment and delivery summaries, and immutable status history.")
    public OrderDetailResponse get(@PathVariable UUID publicId) {
        return orderService.get(publicId);
    }

    @PatchMapping("/{publicId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Transition an order status",
            description = "Validates lifecycle rules and records every change. Terminal CANCELLED and REFUNDED orders cannot be reactivated."
    )
    public OrderDetailResponse updateStatus(
            @PathVariable UUID publicId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return orderService.updateStatus(publicId, request.status(), request.reason());
    }

    @PostMapping("/{publicId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Cancel an order",
            description = "Requires a reason, records the transition, and sets cancellation and delivery metadata."
    )
    public OrderDetailResponse cancel(
            @PathVariable UUID publicId,
            @Valid @RequestBody OrderCancelRequest request
    ) {
        return orderService.cancel(publicId, request.reason());
    }

    @PostMapping("/{publicId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Refund an order",
            description = "Requires a reason, records the transition, and sets refund and payment metadata."
    )
    public OrderDetailResponse refund(
            @PathVariable UUID publicId,
            @Valid @RequestBody OrderRefundRequest request
    ) {
        return orderService.refund(publicId, request.reason());
    }
}
