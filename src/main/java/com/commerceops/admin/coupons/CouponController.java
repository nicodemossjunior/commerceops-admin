package com.commerceops.admin.coupons;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.coupons.dto.CouponFilter;
import com.commerceops.admin.coupons.dto.CouponRequest;
import com.commerceops.admin.coupons.dto.CouponResponse;
import com.commerceops.admin.coupons.model.CouponStatus;
import com.commerceops.admin.coupons.model.DiscountType;
import com.commerceops.admin.coupons.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Coupons", description = "Promotional coupon configuration and lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class CouponController {

    private static final String READ_ROLES = "hasAnyRole('ADMIN', 'MANAGER', 'SUPPORT', 'READ_ONLY', 'CATALOG')";
    private static final String WRITE_ROLES = "hasAnyRole('ADMIN', 'MANAGER')";

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    @Operation(
            summary = "List and filter coupons",
            description = "Returns non-deleted coupons. The activeAt filter only returns active coupons within their validity and usage windows."
    )
    public PageResponse<CouponResponse> list(
            @Parameter(description = "Case-insensitive partial coupon code")
            @RequestParam(required = false) String code,
            @Parameter(description = "Effective coupon status")
            @RequestParam(required = false) CouponStatus status,
            @Parameter(description = "Discount calculation type")
            @RequestParam(required = false) DiscountType discountType,
            @Parameter(description = "Timestamp at which the coupon must be eligible for use")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant activeAt,
            @ParameterObject Pageable pageable
    ) {
        return couponService.list(new CouponFilter(code, status, discountType, activeAt), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(WRITE_ROLES)
    @Operation(
            summary = "Create a coupon",
            description = "Codes are trimmed and uppercased. Discounts and validity dates must satisfy the documented rules."
    )
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return couponService.create(request);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize(READ_ROLES)
    @Operation(summary = "Get a coupon by public ID", description = "Expired active coupons are represented with EXPIRED status.")
    public CouponResponse get(@PathVariable UUID publicId) {
        return couponService.get(publicId);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize(WRITE_ROLES)
    @Operation(summary = "Update a coupon", description = "Applies the same normalization and validation rules as creation.")
    public CouponResponse update(@PathVariable UUID publicId, @Valid @RequestBody CouponRequest request) {
        return couponService.update(publicId, request);
    }

    @PatchMapping("/{publicId}/activate")
    @PreAuthorize(WRITE_ROLES)
    @Operation(summary = "Activate a coupon", description = "Expired coupons cannot be activated.")
    public CouponResponse activate(@PathVariable UUID publicId) {
        return couponService.activate(publicId);
    }

    @PatchMapping("/{publicId}/deactivate")
    @PreAuthorize(WRITE_ROLES)
    @Operation(summary = "Deactivate a coupon", description = "Inactive coupons are excluded from activeAt queries.")
    public CouponResponse deactivate(@PathVariable UUID publicId) {
        return couponService.deactivate(publicId);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(WRITE_ROLES)
    @Operation(summary = "Soft delete a coupon")
    public void delete(@PathVariable UUID publicId) {
        couponService.delete(publicId);
    }
}
