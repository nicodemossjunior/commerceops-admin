package com.commerceops.admin.customers;

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
class CustomerOpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentsCustomersNotesStatusesAndPurchaseHistory() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/customers']").exists())
                .andExpect(jsonPath("$.paths['/api/customers/{publicId}']").exists())
                .andExpect(jsonPath("$.paths['/api/customers/{customerPublicId}/notes']").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/customers/{customerPublicId}/notes/{notePublicId}']"
                ).exists())
                .andExpect(jsonPath("$.paths['/api/customers/{publicId}/orders']").exists())
                .andExpect(jsonPath("$.paths['/api/customers'].get.parameters[*].name", hasItem("status")))
                .andExpect(jsonPath(
                        "$.components.schemas.CustomerRequest.properties.status.enum",
                        hasItem("BLOCKED")
                ));
    }
}
