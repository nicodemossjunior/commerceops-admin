package com.commerceops.admin.coupons.dto;

import com.commerceops.admin.coupons.model.CouponStatus;
import com.commerceops.admin.coupons.model.DiscountType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public record CouponResponse(
        UUID publicId,
        String code,
        String description,
        @Schema(allowableValues = {"FIXED_AMOUNT", "PERCENTAGE"}) DiscountType discountType,
        BigDecimal discountValue,
        Instant startsAt,
        Instant endsAt,
        Integer usageLimit,
        int usageCount,
        Integer perCustomerLimit,
        @Schema(allowableValues = {"ACTIVE", "INACTIVE", "EXPIRED"}) CouponStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
