package com.commerceops.admin.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.dashboard.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessDashboard() throws Exception {
        expectAccessAllowed();
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanAccessDashboard() throws Exception {
        expectAccessAllowed();
    }

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void readOnlyCanAccessDashboard() throws Exception {
        expectAccessAllowed();
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    void supportCanAccessDashboard() throws Exception {
        expectAccessAllowed();
    }

    @Test
    @WithMockUser(roles = "CATALOG")
    void catalogCannotAccessFullDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isForbidden());
    }

    private void expectAccessAllowed() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk());
    }
}
