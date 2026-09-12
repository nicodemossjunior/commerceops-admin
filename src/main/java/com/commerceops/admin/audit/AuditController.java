package com.commerceops.admin.audit;

import com.commerceops.admin.audit.dto.AuditLogDetailResponse;
import com.commerceops.admin.audit.dto.AuditLogFilter;
import com.commerceops.admin.audit.dto.AuditLogSummaryResponse;
import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.audit.service.AuditService;
import com.commerceops.admin.common.pagination.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Audit Logs", description = "Immutable records of sensitive administrative and domain actions")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(
            summary = "List and filter audit logs",
            description = "Returns immutable audit records ordered by creation time descending by default. "
                    + "Date boundaries are inclusive. ADMIN and MANAGER roles are allowed."
    )
    public PageResponse<AuditLogSummaryResponse> list(
            @Parameter(description = "Actor public UUID")
            @RequestParam(required = false) UUID actorUserId,
            @Parameter(description = "Exact actor email, matched case-insensitively")
            @RequestParam(required = false) String actorEmail,
            @Parameter(description = "Audited action")
            @RequestParam(required = false) AuditAction action,
            @Parameter(description = "Exact target entity type, matched case-insensitively")
            @RequestParam(required = false) String entityType,
            @Parameter(description = "Target entity public UUID")
            @RequestParam(required = false) UUID entityPublicId,
            @Parameter(description = "Inclusive creation timestamp lower bound")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @Parameter(description = "Inclusive creation timestamp upper bound")
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
    @Operation(
            summary = "Get an audit log by public ID",
            description = "Returns the immutable audit record and its redacted metadata. Audit records cannot be updated or deleted through the API."
    )
    public AuditLogDetailResponse get(@PathVariable UUID publicId) {
        return auditService.get(publicId);
    }
}
