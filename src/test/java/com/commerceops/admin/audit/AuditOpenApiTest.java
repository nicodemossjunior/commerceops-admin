package com.commerceops.admin.audit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
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
class AuditOpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentsReadOnlyEndpointsActionsAndRedactionRules() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/audit-logs'].get").exists())
                .andExpect(jsonPath("$.paths['/api/audit-logs/{publicId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/audit-logs'].post").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/audit-logs/{publicId}'].put").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/audit-logs/{publicId}'].delete").doesNotExist())
                .andExpect(jsonPath(
                        "$.paths['/api/audit-logs'].get.parameters[2].schema.enum",
                        hasItems(
                                "AUTH_LOGIN_SUCCESS",
                                "AUTH_LOGIN_FAILURE",
                                "PRODUCT_CREATED",
                                "ORDER_CANCELLED",
                                "COUPON_DEACTIVATED"
                        )
                ))
                .andExpect(jsonPath(
                        "$.paths['/api/audit-logs/{publicId}'].get.description",
                        containsString("cannot be updated or deleted")
                ))
                .andExpect(jsonPath(
                        "$.components.schemas.AuditLogDetailResponse.properties.metadata.description",
                        containsString("password hashes")
                ))
                .andExpect(jsonPath(
                        "$.components.schemas.AuditLogDetailResponse.properties.metadata.description",
                        containsString("[REDACTED]")
                ));
    }
}
