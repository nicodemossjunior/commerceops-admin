package com.commerceops.admin.coupons.dto;

import com.commerceops.admin.coupons.model.CouponStatus;
import com.commerceops.admin.coupons.model.DiscountType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(
        @NotBlank(message = "Coupon code is required.")
        @Size(max = 80, message = "Coupon code must not exceed 80 characters.")
        String code,
        @Size(max = 1000, message = "Coupon description must not exceed 1000 characters.")
        String description,
        @NotNull(message = "Coupon discount type is required.")
        DiscountType discountType,
        @NotNull(message = "Coupon discount value is required.")
        @Digits(integer = 17, fraction = 2, message = "Coupon discount value must have at most 2 decimal places.")
        BigDecimal discountValue,
        Instant startsAt,
        Instant endsAt,
        @Positive(message = "Coupon usage limit must be greater than zero.")
        Integer usageLimit,
        @Positive(message = "Coupon per-customer limit must be greater than zero.")
        Integer perCustomerLimit,
        @NotNull(message = "Coupon status is required.")
        CouponStatus status
) {
}
