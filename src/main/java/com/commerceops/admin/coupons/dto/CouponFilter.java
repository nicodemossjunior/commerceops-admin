package com.commerceops.admin.coupons.dto;

import com.commerceops.admin.coupons.model.CouponStatus;
import com.commerceops.admin.coupons.model.DiscountType;
import java.time.Instant;

public record CouponFilter(
        String code,
        CouponStatus status,
        DiscountType discountType,
        Instant activeAt
) {
}
