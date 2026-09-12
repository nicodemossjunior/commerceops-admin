package com.commerceops.admin.audit;

import com.commerceops.admin.audit.dto.AuditLogDetailResponse;
import com.commerceops.admin.audit.dto.AuditLogFilter;
import com.commerceops.admin.audit.dto.AuditLogSummaryResponse;
import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.audit.service.AuditService;
import com.commerceops.admin.common.pagination.PageResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public PageResponse<AuditLogSummaryResponse> list(
            @RequestParam(required = false) UUID actorUserId,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityPublicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            Pageable pageable
    ) {
        return auditService.list(
                new AuditLogFilter(
                        actorUserId,
                        actorEmail,
                        action,
                        entityType,
                        entityPublicId,
                        createdFrom,
                        createdTo
                ),
                pageable
        );
    }

    @GetMapping("/{publicId}")
    public AuditLogDetailResponse get(@PathVariable UUID publicId) {
        return auditService.get(publicId);
    }
}
