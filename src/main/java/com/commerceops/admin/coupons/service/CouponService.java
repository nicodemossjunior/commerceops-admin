package com.commerceops.admin.coupons.service;

import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.audit.service.AuditRecorder;
import com.commerceops.admin.common.error.BusinessRuleException;
import com.commerceops.admin.common.error.DuplicateResourceException;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.common.security.CurrentUserProvider;
import com.commerceops.admin.coupons.dto.CouponFilter;
import com.commerceops.admin.coupons.dto.CouponRequest;
import com.commerceops.admin.coupons.dto.CouponResponse;
import com.commerceops.admin.coupons.model.Coupon;
import com.commerceops.admin.coupons.repository.CouponRepository;
import com.commerceops.admin.coupons.repository.CouponSpecifications;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AuditRecorder auditRecorder;

    public CouponService(
            CouponRepository couponRepository,
            CurrentUserProvider currentUserProvider,
            AuditRecorder auditRecorder
    ) {
        this.couponRepository = couponRepository;
        this.currentUserProvider = currentUserProvider;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        String code = normalizeCode(request.code());
        ensureCodeAvailable(code, null);
        Coupon coupon = new Coupon(
                code,
                normalizeOptional(request.description()),
                request.discountType(),
                request.discountValue(),
                request.startsAt(),
                request.endsAt(),
                request.usageLimit(),
                request.perCustomerLimit(),
                request.status()
        );
        Coupon savedCoupon = couponRepository.save(coupon);
        auditRecorder.record(
                AuditAction.COUPON_CREATED,
                "COUPON",
                savedCoupon.getPublicId(),
                Map.of("code", savedCoupon.getCode(), "status", savedCoupon.getStatus().name())
        );
        return toResponse(savedCoupon);
    }

    @Transactional(readOnly = true)
    public CouponResponse get(UUID publicId) {
        return toResponse(findActive(publicId));
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> list(CouponFilter filter, Pageable pageable) {
        Instant now = Instant.now();
        return PageResponse.from(
                couponRepository.findAll(CouponSpecifications.withFilters(filter, now), pageable)
                        .map(coupon -> toResponse(coupon, now))
        );
    }

    @Transactional
    public CouponResponse update(UUID publicId, CouponRequest request) {
        Coupon coupon = findActive(publicId);
        String code = normalizeCode(request.code());
        ensureCodeAvailable(code, coupon.getId());
        coupon.update(
                code,
                normalizeOptional(request.description()),
                request.discountType(),
                request.discountValue(),
                request.startsAt(),
                request.endsAt(),
                request.usageLimit(),
                request.perCustomerLimit(),
                request.status()
        );
        auditRecorder.record(
                AuditAction.COUPON_UPDATED,
                "COUPON",
                coupon.getPublicId(),
                Map.of("code", coupon.getCode(), "status", coupon.getStatus().name())
        );
        return toResponse(coupon);
    }

    @Transactional
    public CouponResponse activate(UUID publicId) {
        Coupon coupon = findActive(publicId);
        Instant now = Instant.now();
        if (coupon.getEndsAt() != null && !coupon.getEndsAt().isAfter(now)) {
            throw new BusinessRuleException("Expired coupon cannot be activated.");
        }
        coupon.activate();
        auditRecorder.record(
                AuditAction.COUPON_ACTIVATED,
                "COUPON",
                coupon.getPublicId(),
                Map.of("code", coupon.getCode())
        );
        return toResponse(coupon, now);
    }

    @Transactional
    public CouponResponse deactivate(UUID publicId) {
        Coupon coupon = findActive(publicId);
        coupon.deactivate();
        auditRecorder.record(
                AuditAction.COUPON_DEACTIVATED,
                "COUPON",
                coupon.getPublicId(),
                Map.of("code", coupon.getCode())
        );
        return toResponse(coupon);
    }

    @Transactional
    public void delete(UUID publicId) {
        Coupon coupon = findActive(publicId);
        coupon.markDeleted(currentUserProvider.currentUser().id());
        auditRecorder.record(
                AuditAction.COUPON_DELETED,
                "COUPON",
                coupon.getPublicId(),
                Map.of("code", coupon.getCode())
        );
    }

    private Coupon findActive(UUID publicId) {
        return couponRepository.findByPublicIdAndDeletedFalse(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon was not found."));
    }

    private void ensureCodeAvailable(String code, Long currentId) {
        boolean duplicate = currentId == null
                ? couponRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)
                : couponRepository.existsByCodeIgnoreCaseAndDeletedFalseAndIdNot(code, currentId);
        if (duplicate) {
            throw new DuplicateResourceException("Coupon code is already in use.");
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CouponResponse toResponse(Coupon coupon) {
        return toResponse(coupon, Instant.now());
    }

    private CouponResponse toResponse(Coupon coupon, Instant at) {
        return new CouponResponse(
                coupon.getPublicId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getStartsAt(),
                coupon.getEndsAt(),
                coupon.getUsageLimit(),
                coupon.getUsageCount(),
                coupon.getPerCustomerLimit(),
                coupon.effectiveStatus(at),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }
}
