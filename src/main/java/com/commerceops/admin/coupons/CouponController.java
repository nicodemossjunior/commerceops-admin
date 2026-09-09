package com.commerceops.admin.coupons;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.coupons.dto.CouponFilter;
import com.commerceops.admin.coupons.dto.CouponRequest;
import com.commerceops.admin.coupons.dto.CouponResponse;
import com.commerceops.admin.coupons.model.CouponStatus;
import com.commerceops.admin.coupons.model.DiscountType;
import com.commerceops.admin.coupons.service.CouponService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private static final String READ_ROLES = "hasAnyRole('ADMIN', 'MANAGER', 'SUPPORT', 'READ_ONLY', 'CATALOG')";
    private static final String WRITE_ROLES = "hasAnyRole('ADMIN', 'MANAGER')";

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public PageResponse<CouponResponse> list(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) CouponStatus status,
            @RequestParam(required = false) DiscountType discountType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant activeAt,
            Pageable pageable
    ) {
        return couponService.list(new CouponFilter(code, status, discountType, activeAt), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(WRITE_ROLES)
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return couponService.create(request);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize(READ_ROLES)
    public CouponResponse get(@PathVariable UUID publicId) {
        return couponService.get(publicId);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize(WRITE_ROLES)
    public CouponResponse update(@PathVariable UUID publicId, @Valid @RequestBody CouponRequest request) {
        return couponService.update(publicId, request);
    }

    @PatchMapping("/{publicId}/activate")
    @PreAuthorize(WRITE_ROLES)
    public CouponResponse activate(@PathVariable UUID publicId) {
        return couponService.activate(publicId);
    }

    @PatchMapping("/{publicId}/deactivate")
    @PreAuthorize(WRITE_ROLES)
    public CouponResponse deactivate(@PathVariable UUID publicId) {
        return couponService.deactivate(publicId);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(WRITE_ROLES)
    public void delete(@PathVariable UUID publicId) {
        couponService.delete(publicId);
    }
}
