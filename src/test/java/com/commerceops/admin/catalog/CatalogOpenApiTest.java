package com.commerceops.admin.catalog;

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
class CatalogOpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentsCatalogEndpointsFiltersAndStatuses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/categories']").exists())
                .andExpect(jsonPath("$.paths['/api/categories/{publicId}']").exists())
                .andExpect(jsonPath("$.paths['/api/products']").exists())
                .andExpect(jsonPath("$.paths['/api/products/{publicId}']").exists())
                .andExpect(jsonPath("$.paths['/api/products'].get.parameters[*].name", hasItem("categoryId")))
                .andExpect(jsonPath("$.paths['/api/products'].get.parameters[*].name", hasItem("lowStock")))
                .andExpect(jsonPath(
                        "$.components.schemas.ProductRequest.properties.status.enum",
                        hasItem("OUT_OF_STOCK")
                ));
    }
}
