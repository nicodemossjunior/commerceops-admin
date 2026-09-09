package com.commerceops.admin.coupons;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerceops.admin.coupons.dto.CouponFilter;
import com.commerceops.admin.coupons.model.Coupon;
import com.commerceops.admin.coupons.model.CouponStatus;
import com.commerceops.admin.coupons.model.DiscountType;
import com.commerceops.admin.coupons.repository.CouponRepository;
import com.commerceops.admin.coupons.repository.CouponSpecifications;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CouponRepositoryTest {

    private static final Instant REFERENCE_TIME = Instant.parse("2026-09-09T12:00:00Z");

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void combinesFiltersAndExcludesDeletedCoupons() {
        saveCoupon(
                "WELCOME10",
                DiscountType.FIXED_AMOUNT,
                CouponStatus.ACTIVE,
                REFERENCE_TIME.minusSeconds(3600),
                REFERENCE_TIME.plusSeconds(3600)
        );
        saveCoupon("SUMMER20", DiscountType.PERCENTAGE, CouponStatus.INACTIVE, null, null);
        Coupon deleted = saveCoupon("DELETED10", DiscountType.FIXED_AMOUNT, CouponStatus.ACTIVE, null, null);
        deleted.markDeleted(1L);
        couponRepository.flush();

        CouponFilter filter = new CouponFilter(
                "welcome",
                CouponStatus.ACTIVE,
                DiscountType.FIXED_AMOUNT,
                REFERENCE_TIME
        );
        var result = couponRepository.findAll(
                CouponSpecifications.withFilters(filter, REFERENCE_TIME),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Coupon::getCode).containsExactly("WELCOME10");
    }

    @Test
    void resolvesExpiredActiveCouponsInStatusFilter() {
        saveCoupon(
                "EXPIRED10",
                DiscountType.FIXED_AMOUNT,
                CouponStatus.ACTIVE,
                REFERENCE_TIME.minusSeconds(7200),
                REFERENCE_TIME.minusSeconds(3600)
        );
        saveCoupon("ACTIVE10", DiscountType.FIXED_AMOUNT, CouponStatus.ACTIVE, null, null);

        var result = couponRepository.findAll(
                CouponSpecifications.withFilters(
                        new CouponFilter(null, CouponStatus.EXPIRED, null, null),
                        REFERENCE_TIME
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).extracting(Coupon::getCode).containsExactly("EXPIRED10");
    }

    private Coupon saveCoupon(
            String code,
            DiscountType type,
            CouponStatus status,
            Instant startsAt,
            Instant endsAt
    ) {
        return couponRepository.saveAndFlush(new Coupon(
                code,
                null,
                type,
                new BigDecimal("10.00"),
                startsAt,
                endsAt,
                100,
                1,
                status
        ));
    }
}
