package com.commerceops.admin.coupons.dto;

import com.commerceops.admin.coupons.model.CouponStatus;
import com.commerceops.admin.coupons.model.DiscountType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
        UUID publicId,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        Instant startsAt,
        Instant endsAt,
        Integer usageLimit,
        int usageCount,
        Integer perCustomerLimit,
        CouponStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
