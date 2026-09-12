package com.commerceops.admin.audit.model;

import com.commerceops.admin.auth.model.AdminUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(name = "actor_user_id", updatable = false)
    private Long actorUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", insertable = false, updatable = false)
    private AdminUser actorUser;

    @Column(name = "actor_email", length = 190, updatable = false)
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60, updatable = false)
    private AuditAction action;

    @Column(name = "entity_type", length = 100, updatable = false)
    private String entityType;

    @Column(name = "entity_public_id", updatable = false)
    private UUID entityPublicId;

    @Column(name = "trace_id", length = 100, updatable = false)
    private String traceId;

    @Column(name = "request_method", length = 20, updatable = false)
    private String requestMethod;

    @Column(name = "request_path", length = 1000, updatable = false)
    private String requestPath;

    @Column(name = "metadata_json", columnDefinition = "TEXT", updatable = false)
    private String metadataJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuditLog() {
    }

    public AuditLog(
            Long actorUserId,
            String actorEmail,
            AuditAction action,
            String entityType,
            UUID entityPublicId,
            String traceId,
            String requestMethod,
            String requestPath,
            String metadataJson
    ) {
        this.actorUserId = actorUserId;
        this.actorEmail = actorEmail;
        this.action = action;
        this.entityType = entityType;
        this.entityPublicId = entityPublicId;
        this.traceId = traceId;
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.metadataJson = metadataJson;
    }

    @PrePersist
    void onCreate() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public AdminUser getActorUser() {
        return actorUser;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityPublicId() {
        return entityPublicId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
