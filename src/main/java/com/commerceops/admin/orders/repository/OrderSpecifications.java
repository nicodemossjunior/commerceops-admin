package com.commerceops.admin.orders.repository;

import com.commerceops.admin.orders.dto.OrderFilter;
import com.commerceops.admin.orders.model.SalesOrder;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<SalesOrder> withFilters(OrderFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
            if (hasText(filter.orderNumber())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("orderNumber")),
                        "%" + filter.orderNumber().trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }
            if (filter.customerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("publicId"), filter.customerId()));
            }
            if (filter.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
            }
            if (filter.paymentStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), filter.paymentStatus()));
            }
            if (filter.deliveryStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("deliveryStatus"), filter.deliveryStatus()));
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
