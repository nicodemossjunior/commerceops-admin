package com.commerceops.admin.coupons.repository;

import com.commerceops.admin.coupons.dto.CouponFilter;
import com.commerceops.admin.coupons.model.Coupon;
import com.commerceops.admin.coupons.model.CouponStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class CouponSpecifications {

    private CouponSpecifications() {
    }

    public static Specification<Coupon> withFilters(CouponFilter filter, Instant now) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            if (hasText(filter.code())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        "%" + filter.code().trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }
            if (filter.discountType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("discountType"), filter.discountType()));
            }
            if (filter.status() != null) {
                predicates.add(statusPredicate(filter.status(), now, root, criteriaBuilder));
            }
            if (filter.activeAt() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), CouponStatus.ACTIVE));
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("startsAt")),
                        criteriaBuilder.lessThanOrEqualTo(root.get("startsAt"), filter.activeAt())
                ));
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("endsAt")),
                        criteriaBuilder.greaterThan(root.get("endsAt"), filter.activeAt())
                ));
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("usageLimit")),
                        criteriaBuilder.lessThan(root.get("usageCount"), root.get("usageLimit"))
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Predicate statusPredicate(
            CouponStatus status,
            Instant now,
            jakarta.persistence.criteria.Root<Coupon> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder
    ) {
        if (status == CouponStatus.ACTIVE) {
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("status"), CouponStatus.ACTIVE),
                    criteriaBuilder.or(
                            criteriaBuilder.isNull(root.get("endsAt")),
                            criteriaBuilder.greaterThan(root.get("endsAt"), now)
                    )
            );
        }
        if (status == CouponStatus.EXPIRED) {
            return criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("status"), CouponStatus.EXPIRED),
                    criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("status"), CouponStatus.ACTIVE),
                            criteriaBuilder.lessThanOrEqualTo(root.get("endsAt"), now)
                    )
            );
        }
        return criteriaBuilder.equal(root.get("status"), CouponStatus.INACTIVE);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
