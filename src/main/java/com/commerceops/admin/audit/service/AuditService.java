package com.commerceops.admin.audit.service;

import com.commerceops.admin.audit.dto.AuditLogDetailResponse;
import com.commerceops.admin.audit.dto.AuditLogFilter;
import com.commerceops.admin.audit.dto.AuditLogSummaryResponse;
import com.commerceops.admin.audit.model.AuditLog;
import com.commerceops.admin.audit.repository.AuditLogRepository;
import com.commerceops.admin.audit.repository.AuditLogSpecifications;
import com.commerceops.admin.common.error.RequestValidationException;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import com.commerceops.admin.common.pagination.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogSummaryResponse> list(AuditLogFilter filter, Pageable pageable) {
        validateDateRange(filter);
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(
                auditLogRepository.findAll(AuditLogSpecifications.withFilters(filter), effectivePageable)
                        .map(this::toSummary)
        );
    }

    @Transactional(readOnly = true)
    public AuditLogDetailResponse get(UUID publicId) {
        AuditLog auditLog = auditLogRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log was not found."));
        return toDetail(auditLog);
    }

    private void validateDateRange(AuditLogFilter filter) {
        if (filter.createdFrom() != null
                && filter.createdTo() != null
                && filter.createdFrom().isAfter(filter.createdTo())) {
            throw new RequestValidationException("Created-from date cannot be after created-to date.");
        }
    }

    private AuditLogSummaryResponse toSummary(AuditLog auditLog) {
        return new AuditLogSummaryResponse(
                auditLog.getPublicId(),
                actorPublicId(auditLog),
                auditLog.getActorEmail(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityPublicId(),
                auditLog.getTraceId(),
                auditLog.getRequestMethod(),
                auditLog.getRequestPath(),
                auditLog.getCreatedAt()
        );
    }

    private AuditLogDetailResponse toDetail(AuditLog auditLog) {
        return new AuditLogDetailResponse(
                auditLog.getPublicId(),
                actorPublicId(auditLog),
                auditLog.getActorEmail(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityPublicId(),
                auditLog.getTraceId(),
                auditLog.getRequestMethod(),
                auditLog.getRequestPath(),
                parseMetadata(auditLog.getMetadataJson()),
                auditLog.getCreatedAt()
        );
    }

    private UUID actorPublicId(AuditLog auditLog) {
        return auditLog.getActorUser() == null ? null : auditLog.getActorUser().getPublicId();
    }

    private JsonNode parseMetadata(String metadataJson) {
        if (metadataJson == null) {
            return NullNode.getInstance();
        }
        try {
            return objectMapper.readTree(metadataJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored audit metadata is invalid.", exception);
        }
    }
}
