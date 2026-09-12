package com.commerceops.admin.audit.repository;

import com.commerceops.admin.audit.dto.AuditLogFilter;
import com.commerceops.admin.audit.model.AuditLog;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> withFilters(AuditLogFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.actorUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("actorUser").get("publicId"),
                        filter.actorUserId()
                ));
            }
            if (hasText(filter.actorEmail())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("actorEmail")),
                        filter.actorEmail().trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (filter.action() != null) {
                predicates.add(criteriaBuilder.equal(root.get("action"), filter.action()));
            }
            if (hasText(filter.entityType())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("entityType")),
                        filter.entityType().trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (filter.entityPublicId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("entityPublicId"), filter.entityPublicId()));
            }
            if (filter.createdFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.createdFrom()));
            }
            if (filter.createdTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.createdTo()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
