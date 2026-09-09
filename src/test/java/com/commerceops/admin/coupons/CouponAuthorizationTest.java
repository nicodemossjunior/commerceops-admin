package com.commerceops.admin.coupons;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.coupons.dto.CouponResponse;
import com.commerceops.admin.coupons.service.CouponService;
import java.util.List;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CouponAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponService couponService;

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void readOnlyCanReadButCannotCreateCoupons() throws Exception {
        assertReadAccess();

        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCouponJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    void supportCanReadButCannotActivateCoupons() throws Exception {
        assertReadAccess();

        mockMvc.perform(patch("/api/coupons/{publicId}/activate", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CATALOG")
    void catalogCanReadButCannotDeleteCoupons() throws Exception {
        assertReadAccess();

        mockMvc.perform(delete("/api/coupons/{publicId}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanManageCoupons() throws Exception {
        UUID publicId = UUID.randomUUID();

        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCouponJson()))
                .andExpect(status().isCreated());
        mockMvc.perform(patch("/api/coupons/{publicId}/activate", publicId))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/coupons/{publicId}/deactivate", publicId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/coupons/{publicId}", publicId))
                .andExpect(status().isNoContent());
    }

    private void assertReadAccess() throws Exception {
        when(couponService.list(any(), any())).thenReturn(emptyPage());
        mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isOk());
    }

    private PageResponse<CouponResponse> emptyPage() {
        return new PageResponse<>(List.of(), 0, 20, 0, 0, true, true);
    }

    private String validCouponJson() {
        return """
                {
                  "code": "WELCOME10",
                  "discountType": "FIXED_AMOUNT",
                  "discountValue": 10.00,
                  "status": "ACTIVE"
                }
                """;
    }
}
