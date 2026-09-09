package com.commerceops.admin.coupons;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CouponOpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentsCouponEndpointsFiltersStatusesDiscountTypesAndRules() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/coupons'].get").exists())
                .andExpect(jsonPath("$.paths['/api/coupons'].post").exists())
                .andExpect(jsonPath("$.paths['/api/coupons/{publicId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/coupons/{publicId}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/coupons/{publicId}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/coupons/{publicId}/activate'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/coupons/{publicId}/deactivate'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/coupons'].get.parameters[*].name", hasItem("activeAt")))
                .andExpect(jsonPath(
                        "$.components.schemas.CouponRequest.properties.discountType.enum",
                        hasItem("PERCENTAGE")
                ))
                .andExpect(jsonPath(
                        "$.components.schemas.CouponRequest.properties.status.enum",
                        hasItem("EXPIRED")
                ))
                .andExpect(jsonPath(
                        "$.components.schemas.CouponRequest.properties.discountValue.description",
                        containsString("cannot exceed 100")
                ));
    }
}
