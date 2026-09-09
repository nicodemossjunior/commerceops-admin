package com.commerceops.admin.catalog.repository;

import com.commerceops.admin.catalog.dto.ProductFilter;
import com.commerceops.admin.catalog.model.Product;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    public static final int LOW_STOCK_THRESHOLD = 10;

    private ProductSpecifications() {
    }

    public static Specification<Product> withFilters(ProductFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            if (filter.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("publicId"), filter.categoryId()));
            }
            if (filter.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
            }
            if (hasText(filter.sku())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sku")),
                        contains(filter.sku())
                ));
            }
            if (hasText(filter.name())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        contains(filter.name())
                ));
            }
            if (filter.minPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }
            if (Boolean.TRUE.equals(filter.lowStock())) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("stockQuantity"), LOW_STOCK_THRESHOLD));
            } else if (Boolean.FALSE.equals(filter.lowStock())) {
                predicates.add(criteriaBuilder.greaterThan(root.get("stockQuantity"), LOW_STOCK_THRESHOLD));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String contains(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
