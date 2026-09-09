package com.commerceops.admin.orders;

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
class OrderOpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentsOrderEndpointsFiltersStatusesAndOperationalRules() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/orders'].get").exists())
                .andExpect(jsonPath("$.paths['/api/orders/{publicId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/orders/{publicId}/status'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/orders/{publicId}/cancel'].post").exists())
                .andExpect(jsonPath("$.paths['/api/orders/{publicId}/refund'].post").exists())
                .andExpect(jsonPath("$.paths['/api/orders'].get.parameters[*].name", hasItem("customerId")))
                .andExpect(jsonPath("$.paths['/api/orders'].get.parameters[*].name", hasItem("createdFrom")))
                .andExpect(jsonPath(
                        "$.components.schemas.OrderStatusUpdateRequest.properties.status.enum",
                        hasItem("REFUNDED")
                ))
                .andExpect(jsonPath(
                        "$.paths['/api/orders/{publicId}/status'].patch.description",
                        containsString("Terminal CANCELLED and REFUNDED")
                ))
                .andExpect(jsonPath(
                        "$.paths['/api/orders/{publicId}/cancel'].post.description",
                        containsString("Requires a reason")
                ))
                .andExpect(jsonPath(
                        "$.paths['/api/orders/{publicId}/refund'].post.description",
                        containsString("Requires a reason")
                ));
    }
}
