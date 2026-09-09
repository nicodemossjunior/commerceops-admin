package com.commerceops.admin.catalog.service;

import com.commerceops.admin.catalog.dto.ProductRequest;
import com.commerceops.admin.catalog.dto.ProductResponse;
import com.commerceops.admin.catalog.model.Category;
import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.catalog.repository.CategoryRepository;
import com.commerceops.admin.catalog.repository.ProductRepository;
import com.commerceops.admin.common.error.DuplicateResourceException;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import com.commerceops.admin.common.pagination.PageResponse;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
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

        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID publicId) {
        return toResponse(findActive(publicId));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> list(Pageable pageable) {
        return PageResponse.from(productRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
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

        return toResponse(product);
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
