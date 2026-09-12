package com.commerceops.admin.catalog.service;

import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.audit.service.AuditRecorder;
import com.commerceops.admin.catalog.dto.ProductRequest;
import com.commerceops.admin.catalog.dto.ProductResponse;
import com.commerceops.admin.catalog.dto.ProductFilter;
import com.commerceops.admin.catalog.model.Category;
import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.catalog.repository.CategoryRepository;
import com.commerceops.admin.catalog.repository.ProductRepository;
import com.commerceops.admin.catalog.repository.ProductSpecifications;
import com.commerceops.admin.common.error.BusinessRuleException;
import com.commerceops.admin.common.error.DuplicateResourceException;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.common.security.CurrentUserProvider;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AuditRecorder auditRecorder;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            CurrentUserProvider currentUserProvider,
            AuditRecorder auditRecorder
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserProvider = currentUserProvider;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = normalizeSku(request.sku());
        ensureSkuAvailable(sku, null);
        Category category = findCategory(request.categoryPublicId());

        Product product = new Product(
                category,
                sku,
                request.name().trim(),
                normalizeSlug(request.slug()),
                normalizeOptional(request.description()),
                request.price(),
                normalizeOptional(request.imageUrl()),
                request.stockQuantity(),
                request.status()
        );

        Product savedProduct = productRepository.save(product);
        auditRecorder.record(
                AuditAction.PRODUCT_CREATED,
                "PRODUCT",
                savedProduct.getPublicId(),
                Map.of("sku", savedProduct.getSku(), "status", savedProduct.getStatus().name())
        );
        return toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID publicId) {
        return toResponse(findActive(publicId));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> list(ProductFilter filter, Pageable pageable) {
        validatePriceRange(filter);
        return PageResponse.from(
                productRepository.findAll(ProductSpecifications.withFilters(filter), pageable).map(this::toResponse)
        );
    }

    @Transactional
    public ProductResponse update(UUID publicId, ProductRequest request) {
        Product product = findActive(publicId);
        String sku = normalizeSku(request.sku());
        ensureSkuAvailable(sku, product.getId());
        Category category = findCategory(request.categoryPublicId());

        product.update(
                category,
                sku,
                request.name().trim(),
                normalizeSlug(request.slug()),
                normalizeOptional(request.description()),
                request.price(),
                normalizeOptional(request.imageUrl()),
                request.stockQuantity(),
                request.status()
        );

        auditRecorder.record(
                AuditAction.PRODUCT_UPDATED,
                "PRODUCT",
                product.getPublicId(),
                Map.of("sku", product.getSku(), "status", product.getStatus().name())
        );

        return toResponse(product);
    }

    @Transactional
    public void delete(UUID publicId) {
        Product product = findActive(publicId);
        product.markDeleted(currentUserProvider.currentUser().id());
        auditRecorder.record(
                AuditAction.PRODUCT_DELETED,
                "PRODUCT",
                product.getPublicId(),
                Map.of("sku", product.getSku())
        );
    }

    private void validatePriceRange(ProductFilter filter) {
        if (filter.minPrice() != null && filter.minPrice().signum() < 0) {
            throw new BusinessRuleException("Minimum price cannot be negative.");
        }
        if (filter.maxPrice() != null && filter.maxPrice().signum() < 0) {
            throw new BusinessRuleException("Maximum price cannot be negative.");
        }
        if (filter.minPrice() != null
                && filter.maxPrice() != null
                && filter.minPrice().compareTo(filter.maxPrice()) > 0) {
            throw new BusinessRuleException("Minimum price cannot be greater than maximum price.");
        }
    }

    private Product findActive(UUID publicId) {
        return productRepository.findByPublicIdAndDeletedFalse(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found."));
    }

    private Category findCategory(UUID publicId) {
        return categoryRepository.findByPublicIdAndDeletedFalse(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Product category was not found."));
    }

    private void ensureSkuAvailable(String sku, Long currentId) {
        boolean duplicate = currentId == null
                ? productRepository.existsBySkuIgnoreCaseAndDeletedFalse(sku)
                : productRepository.existsBySkuIgnoreCaseAndDeletedFalseAndIdNot(sku, currentId);
        if (duplicate) {
            throw new DuplicateResourceException("Product SKU is already in use.");
        }
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getPublicId(),
                product.getCategory().getPublicId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
