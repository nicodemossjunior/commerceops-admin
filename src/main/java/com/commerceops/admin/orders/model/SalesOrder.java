package com.commerceops.admin.orders.model;

import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.common.error.BusinessRuleException;
import com.commerceops.admin.common.persistence.BaseEntity;
import com.commerceops.admin.customers.model.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "sales_order")
public class SalesOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "shipping_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 30)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    protected SalesOrder() {
    }

    public SalesOrder(
            Customer customer,
            String orderNumber,
            BigDecimal subtotalAmount,
            BigDecimal discountAmount,
            BigDecimal shippingAmount,
            BigDecimal totalAmount
    ) {
        this.customer = customer;
        this.orderNumber = orderNumber;
        this.subtotalAmount = subtotalAmount;
        this.discountAmount = discountAmount;
        this.shippingAmount = shippingAmount;
        this.totalAmount = totalAmount;
    }

    public void addItem(Product product, String productSku, String productName, BigDecimal unitPrice, int quantity) {
        items.add(new OrderItem(this, product, productSku, productName, unitPrice, quantity));
    }

    public void addStatusHistory(OrderStatus fromStatus, OrderStatus toStatus, Long changedBy, String reason) {
        statusHistory.add(new OrderStatusHistory(this, fromStatus, toStatus, changedBy, reason));
    }

    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
        if (newStatus == OrderStatus.PAID) {
            paymentStatus = PaymentStatus.PAID;
        } else if (newStatus == OrderStatus.PROCESSING) {
            deliveryStatus = DeliveryStatus.PREPARING;
        } else if (newStatus == OrderStatus.SHIPPED) {
            deliveryStatus = DeliveryStatus.SHIPPED;
        } else if (newStatus == OrderStatus.DELIVERED) {
            deliveryStatus = DeliveryStatus.DELIVERED;
        } else if (newStatus == OrderStatus.CANCELLED) {
            cancelledAt = Instant.now();
            deliveryStatus = DeliveryStatus.CANCELLED;
        } else if (newStatus == OrderStatus.REFUNDED) {
            refundedAt = Instant.now();
            paymentStatus = PaymentStatus.REFUNDED;
        }
    }

    @PrePersist
    void validateOrder() {
        if (items.isEmpty()) {
            throw new BusinessRuleException("Order must contain at least one item.");
        }
        BigDecimal itemSubtotal = items.stream()
                .map(OrderItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (itemSubtotal.compareTo(subtotalAmount) != 0) {
            throw new BusinessRuleException("Order subtotal must match the sum of item totals.");
        }
        BigDecimal calculatedTotal = subtotalAmount.subtract(discountAmount).add(shippingAmount);
        if (calculatedTotal.compareTo(totalAmount) != 0) {
            throw new BusinessRuleException("Order total does not match subtotal, discount, and shipping amounts.");
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }
}
