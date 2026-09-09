package com.commerceops.admin.customers.repository;

import com.commerceops.admin.customers.dto.CustomerFilter;
import com.commerceops.admin.customers.model.Customer;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerSpecifications {

    private CustomerSpecifications() {
    }

    public static Specification<Customer> withFilters(CustomerFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
            addContains(predicates, filter.name(), "name", root, criteriaBuilder);
            addContains(predicates, filter.email(), "email", root, criteriaBuilder);
            addContains(predicates, filter.phone(), "phone", root, criteriaBuilder);
            if (filter.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addContains(
            List<Predicate> predicates,
            String value,
            String field,
            Root<Customer> root,
            CriteriaBuilder criteriaBuilder
    ) {
        if (value != null && !value.isBlank()) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get(field)),
                    "%" + value.trim().toLowerCase(Locale.ROOT) + "%"
            ));
        }
    }
}
