package com.commerceops.admin.catalog.repository;

import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.catalog.model.ProductStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByPublicIdAndDeletedFalse(UUID publicId);

    boolean existsBySkuAndDeletedFalse(String sku);

    boolean existsByCategoryIdAndStatusAndDeletedFalse(Long categoryId, ProductStatus status);
}
