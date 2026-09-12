package com.commerceops.admin.audit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.audit.service.AuditService;
import java.util.UUID;
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
class AuditAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadAuditLogs() throws Exception {
        expectListAccessAllowed();
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanReadAuditLogs() throws Exception {
        expectListAccessAllowed();
    }

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void readOnlyCannotReadAuditLogs() throws Exception {
        expectAccessDenied();
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    void supportCannotReadAuditLogs() throws Exception {
        expectAccessDenied();
    }

    @Test
    @WithMockUser(roles = "CATALOG")
    void catalogCannotReadAuditLogs() throws Exception {
        expectAccessDenied();
    }

    private void expectListAccessAllowed() throws Exception {
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isOk());
    }

    private void expectAccessDenied() throws Exception {
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/audit-logs/{publicId}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
