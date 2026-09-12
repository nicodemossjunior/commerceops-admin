package com.commerceops.admin.audit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.audit.model.AuditLog;
import com.commerceops.admin.audit.repository.AuditLogRepository;
import com.commerceops.admin.auth.model.AdminUser;
import com.commerceops.admin.auth.repository.AdminUserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listsAuditLogsWithCombinedFiltersAndPagination() throws Exception {
        AdminUser actor = saveActor();
        UUID productPublicId = UUID.randomUUID();
        saveLog(actor, AuditAction.PRODUCT_UPDATED, "PRODUCT", productPublicId);
        saveLog(actor, AuditAction.CUSTOMER_UPDATED, "CUSTOMER", UUID.randomUUID());
        entityManager.clear();

        mockMvc.perform(get("/api/audit-logs")
                        .param("actorUserId", actor.getPublicId().toString())
                        .param("actorEmail", actor.getEmail().toUpperCase())
                        .param("action", "PRODUCT_UPDATED")
                        .param("entityType", "product")
                        .param("entityPublicId", productPublicId.toString())
                        .param("createdFrom", Instant.now().minusSeconds(60).toString())
                        .param("createdTo", Instant.now().plusSeconds(60).toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].actorUserId").value(actor.getPublicId().toString()))
                .andExpect(jsonPath("$.content[0].actorEmail").value("audit.actor@example.com"))
                .andExpect(jsonPath("$.content[0].action").value("PRODUCT_UPDATED"))
                .andExpect(jsonPath("$.content[0].entityPublicId").value(productPublicId.toString()));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void returnsAuditDetailByPublicId() throws Exception {
        AdminUser actor = saveActor();
        AuditLog auditLog = saveLog(actor, AuditAction.ORDER_CANCELLED, "ORDER", UUID.randomUUID());
        UUID auditPublicId = auditLog.getPublicId();
        UUID actorPublicId = actor.getPublicId();
        entityManager.clear();

        mockMvc.perform(get("/api/audit-logs/{publicId}", auditPublicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(auditPublicId.toString()))
                .andExpect(jsonPath("$.actorUserId").value(actorPublicId.toString()))
                .andExpect(jsonPath("$.traceId").value("trace-audit-test"))
                .andExpect(jsonPath("$.requestMethod").value("PATCH"))
                .andExpect(jsonPath("$.requestPath").value("/api/orders/example/cancel"))
                .andExpect(jsonPath("$.metadata.reason").value("Customer request"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void returnsExpectedErrorsForInvalidRangeAndMissingLog() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .param("createdFrom", "2026-09-11T12:00:00Z")
                        .param("createdTo", "2026-09-11T11:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/api/audit-logs/{publicId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private AdminUser saveActor() {
        return adminUserRepository.saveAndFlush(
                new AdminUser("Audit Actor", "audit.actor@example.com", "test-password-hash", Set.of())
        );
    }

    private AuditLog saveLog(
            AdminUser actor,
            AuditAction action,
            String entityType,
            UUID entityPublicId
    ) {
        return auditLogRepository.saveAndFlush(new AuditLog(
                actor.getId(),
                actor.getEmail(),
                action,
                entityType,
                entityPublicId,
                "trace-audit-test",
                "PATCH",
                "/api/orders/example/cancel",
                "{\"reason\":\"Customer request\"}"
        ));
    }
}
