package com.commerceops.admin.dashboard;

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
class DashboardOpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentsEndpointPeriodsAndMetricMeanings() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/dashboard/summary'].get").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/dashboard/summary'].get.parameters[0].schema.enum",
                        hasItems("TODAY", "LAST_7_DAYS", "LAST_30_DAYS", "THIS_MONTH", "CUSTOM")
                ))
                .andExpect(jsonPath(
                        "$.paths['/api/dashboard/summary'].get.description",
                        containsString("default period is LAST_30_DAYS")
                ))
                .andExpect(jsonPath(
                        "$.components.schemas.DashboardMetricsResponse.properties.grossRevenue.description",
                        containsString("including refunded orders")
                ))
                .andExpect(jsonPath(
                        "$.components.schemas.DashboardMetricsResponse.properties.netRevenue.description",
                        containsString("excluding cancelled and refunded orders")
                ))
                .andExpect(jsonPath(
                        "$.components.schemas.DashboardMetricsResponse.properties.averageOrderValue.description",
                        containsString("Net revenue divided")
                ))
                .andExpect(jsonPath(
                        "$.components.schemas.DashboardMetricsResponse.properties.customerCount.description",
                        containsString("non-deleted customers")
                ))
                .andExpect(jsonPath(
                        "$.components.schemas.DashboardMetricsResponse.properties.lowStockProductCount.description",
                        containsString("configured stock threshold")
                ));
    }
}
