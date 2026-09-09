package com.commerceops.admin.catalog.repository;

import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.catalog.model.ProductStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByPublicIdAndDeletedFalse(UUID publicId);

    boolean existsBySkuIgnoreCaseAndDeletedFalse(String sku);

    boolean existsBySkuIgnoreCaseAndDeletedFalseAndIdNot(String sku, Long id);

    Page<Product> findAllByDeletedFalse(Pageable pageable);

    boolean existsByCategoryIdAndStatusAndDeletedFalse(Long categoryId, ProductStatus status);
}
