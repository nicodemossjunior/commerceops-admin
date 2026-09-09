package com.commerceops.admin.coupons.model;

import com.commerceops.admin.common.error.BusinessRuleException;
import com.commerceops.admin.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupon")
public class Coupon extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String code;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 30)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @Column(name = "per_customer_limit")
    private Integer perCustomerLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponStatus status;

    protected Coupon() {
    }

    public Coupon(
            String code,
            String description,
            DiscountType discountType,
            BigDecimal discountValue,
            Instant startsAt,
            Instant endsAt,
            Integer usageLimit,
            Integer perCustomerLimit,
            CouponStatus status
    ) {
        update(code, description, discountType, discountValue, startsAt, endsAt, usageLimit, perCustomerLimit, status);
    }

    public void update(
            String code,
            String description,
            DiscountType discountType,
            BigDecimal discountValue,
            Instant startsAt,
            Instant endsAt,
            Integer usageLimit,
            Integer perCustomerLimit,
            CouponStatus status
    ) {
        validate(discountType, discountValue, startsAt, endsAt, usageLimit, perCustomerLimit);
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.usageLimit = usageLimit;
        this.perCustomerLimit = perCustomerLimit;
        this.status = status;
    }

    public void activate() {
        status = CouponStatus.ACTIVE;
    }

    public void deactivate() {
        status = CouponStatus.INACTIVE;
    }

    public CouponStatus effectiveStatus(Instant at) {
        if (endsAt != null && endsAt.isBefore(at)) {
            return CouponStatus.EXPIRED;
        }
        return status;
    }

    @PrePersist
    @PreUpdate
    void validateState() {
        validate(discountType, discountValue, startsAt, endsAt, usageLimit, perCustomerLimit);
    }

    private void validate(
            DiscountType type,
            BigDecimal value,
            Instant start,
            Instant end,
            Integer totalLimit,
            Integer customerLimit
    ) {
        if (type != null && value != null) {
            if (value.signum() <= 0) {
                throw new BusinessRuleException("Coupon discount value must be greater than zero.");
            }
            if (type == DiscountType.PERCENTAGE && value.compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessRuleException("Percentage discount must not exceed 100.");
            }
        }
        if (start != null && end != null && !end.isAfter(start)) {
            throw new BusinessRuleException("Coupon end date must be after its start date.");
        }
        if (totalLimit != null && totalLimit <= 0) {
            throw new BusinessRuleException("Coupon usage limit must be greater than zero.");
        }
        if (customerLimit != null && customerLimit <= 0) {
            throw new BusinessRuleException("Coupon per-customer limit must be greater than zero.");
        }
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public Integer getPerCustomerLimit() {
        return perCustomerLimit;
    }

    public CouponStatus getStatus() {
        return status;
    }
}
