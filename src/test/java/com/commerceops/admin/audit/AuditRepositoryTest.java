package com.commerceops.admin.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerceops.admin.audit.dto.AuditLogFilter;
import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.audit.model.AuditLog;
import com.commerceops.admin.audit.repository.AuditLogRepository;
import com.commerceops.admin.audit.repository.AuditLogSpecifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void filtersAuditLogsByActionEntityAndInclusiveDates() {
        UUID entityPublicId = UUID.randomUUID();
        AuditLog expected = new AuditLog(
                null,
                "system@example.com",
                AuditAction.AUTH_LOGIN_FAILURE,
                "ADMIN_USER",
                entityPublicId,
                null,
                "POST",
                "/api/auth/login",
                null
        );
        ReflectionTestUtils.setField(expected, "createdAt", Instant.parse("2026-08-12T16:21:00.123456789Z"));
        auditLogRepository.saveAndFlush(expected);
        entityManager.refresh(expected);

        auditLogRepository.saveAndFlush(new AuditLog(
                null,
                "system@example.com",
                AuditAction.COUPON_UPDATED,
                "COUPON",
                UUID.randomUUID(),
                null,
                "PUT",
                "/api/coupons/example",
                null
        ));

        AuditLogFilter filter = new AuditLogFilter(
                null,
                "SYSTEM@EXAMPLE.COM",
                AuditAction.AUTH_LOGIN_FAILURE,
                "admin_user",
                entityPublicId,
                expected.getCreatedAt(),
                expected.getCreatedAt()
        );
        var result = auditLogRepository.findAll(
                AuditLogSpecifications.withFilters(filter),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(AuditLog::getPublicId)
                .containsExactly(expected.getPublicId());
    }
}
