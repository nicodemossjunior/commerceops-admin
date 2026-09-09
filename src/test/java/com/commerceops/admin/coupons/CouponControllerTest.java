package com.commerceops.admin.coupons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.common.security.CurrentUser;
import com.commerceops.admin.common.security.CurrentUserProvider;
import com.commerceops.admin.coupons.model.Coupon;
import com.commerceops.admin.coupons.model.CouponStatus;
import com.commerceops.admin.coupons.model.DiscountType;
import com.commerceops.admin.coupons.repository.CouponRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createsCouponAndNormalizesCode() throws Exception {
        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponJson("  welcome10  ", "FIXED_AMOUNT", "10.00", "ACTIVE", null, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.code").value("WELCOME10"))
                .andExpect(jsonPath("$.discountType").value("FIXED_AMOUNT"))
                .andExpect(jsonPath("$.discountValue").value(10.00))
                .andExpect(jsonPath("$.usageCount").value(0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updatesCoupon() throws Exception {
        Coupon coupon = saveCoupon("WELCOME10", DiscountType.FIXED_AMOUNT, "10.00", CouponStatus.ACTIVE);

        mockMvc.perform(put("/api/coupons/{publicId}", coupon.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponJson("summer20", "PERCENTAGE", "20.00", "INACTIVE", null, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(coupon.getPublicId().toString()))
                .andExpect(jsonPath("$.code").value("SUMMER20"))
                .andExpect(jsonPath("$.discountType").value("PERCENTAGE"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void filtersCouponListingByCodeStatusTypeAndActiveDate() throws Exception {
        saveCoupon("WELCOME10", DiscountType.FIXED_AMOUNT, "10.00", CouponStatus.ACTIVE);
        saveCoupon("SUMMER20", DiscountType.PERCENTAGE, "20.00", CouponStatus.INACTIVE);

        mockMvc.perform(get("/api/coupons")
                        .param("code", "welcome")
                        .param("status", "ACTIVE")
                        .param("discountType", "FIXED_AMOUNT")
                        .param("activeAt", Instant.now().toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value("WELCOME10"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsDuplicateCodeIgnoringCase() throws Exception {
        saveCoupon("WELCOME10", DiscountType.FIXED_AMOUNT, "10.00", CouponStatus.ACTIVE);

        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponJson("welcome10", "FIXED_AMOUNT", "5.00", "ACTIVE", null, null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsNonPositiveFixedAmountDiscount() throws Exception {
        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponJson("INVALID", "FIXED_AMOUNT", "0.00", "ACTIVE", null, null)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsPercentageDiscountAboveOneHundred() throws Exception {
        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponJson("INVALID", "PERCENTAGE", "100.01", "ACTIVE", null, null)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsInvalidDateRange() throws Exception {
        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponJson(
                                "INVALID",
                                "FIXED_AMOUNT",
                                "10.00",
                                "ACTIVE",
                                "2026-10-02T00:00:00Z",
                                "2026-10-01T00:00:00Z"
                        )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void activatesAndDeactivatesCoupon() throws Exception {
        Coupon coupon = saveCoupon("WELCOME10", DiscountType.FIXED_AMOUNT, "10.00", CouponStatus.INACTIVE);

        mockMvc.perform(patch("/api/coupons/{publicId}/activate", coupon.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(patch("/api/coupons/{publicId}/deactivate", coupon.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void representsExpiredCouponAndRejectsActivation() throws Exception {
        Coupon coupon = couponRepository.saveAndFlush(new Coupon(
                "EXPIRED10",
                null,
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("10.00"),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                null,
                null,
                CouponStatus.ACTIVE
        ));

        mockMvc.perform(get("/api/coupons/{publicId}", coupon.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));

        mockMvc.perform(patch("/api/coupons/{publicId}/activate", coupon.getPublicId()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void softDeletesCoupon() throws Exception {
        Coupon coupon = saveCoupon("WELCOME10", DiscountType.FIXED_AMOUNT, "10.00", CouponStatus.ACTIVE);
        when(currentUserProvider.currentUser())
                .thenReturn(new CurrentUser(42L, UUID.randomUUID(), "admin@example.com", Set.of("ADMIN")));

        mockMvc.perform(delete("/api/coupons/{publicId}", coupon.getPublicId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/coupons/{publicId}", coupon.getPublicId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        Coupon deleted = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedBy()).isEqualTo(42L);

        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponJson("welcome10", "FIXED_AMOUNT", "5.00", "ACTIVE", null, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WELCOME10"));
    }

    private Coupon saveCoupon(String code, DiscountType type, String value, CouponStatus status) {
        return couponRepository.saveAndFlush(new Coupon(
                code,
                "Promotional coupon",
                type,
                new BigDecimal(value),
                null,
                null,
                100,
                1,
                status
        ));
    }

    private String couponJson(
            String code,
            String discountType,
            String discountValue,
            String status,
            String startsAt,
            String endsAt
    ) {
        return """
                {
                  "code": "%s",
                  "description": "Promotional coupon",
                  "discountType": "%s",
                  "discountValue": %s,
                  "startsAt": %s,
                  "endsAt": %s,
                  "usageLimit": 100,
                  "perCustomerLimit": 1,
                  "status": "%s"
                }
                """.formatted(
                code,
                discountType,
                discountValue,
                jsonString(startsAt),
                jsonString(endsAt),
                status
        );
    }

    private String jsonString(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
