package com.commerceops.admin.catalog.service;

import com.commerceops.admin.catalog.dto.CategoryRequest;
import com.commerceops.admin.catalog.dto.CategoryResponse;
import com.commerceops.admin.catalog.model.Category;
import com.commerceops.admin.catalog.repository.CategoryRepository;
import com.commerceops.admin.common.error.BusinessRuleException;
import com.commerceops.admin.common.error.DuplicateResourceException;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import com.commerceops.admin.common.pagination.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = normalizeSlug(request.slug());
        ensureSlugAvailable(slug, null);
        Category parent = resolveParent(request.parentPublicId());

        Category category = new Category(
                request.name().trim(),
                slug,
                normalizeOptional(request.description()),
                request.status(),
                parent
        );

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID publicId) {
        return toResponse(findActive(publicId));
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> list(Pageable pageable) {
        return PageResponse.from(categoryRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional
    public CategoryResponse update(UUID publicId, CategoryRequest request) {
        Category category = findActive(publicId);
        String slug = normalizeSlug(request.slug());
        ensureSlugAvailable(slug, category.getId());
        Category parent = resolveParent(request.parentPublicId());

        if (parent != null && parent.getId().equals(category.getId())) {
            throw new BusinessRuleException("A category cannot be its own parent.");
        }

        category.update(
                request.name().trim(),
                slug,
                normalizeOptional(request.description()),
                request.status(),
                parent
        );

        return toResponse(category);
    }

    private Category findActive(UUID publicId) {
        return categoryRepository.findByPublicIdAndDeletedFalse(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Category was not found."));
    }

    private Category resolveParent(UUID parentPublicId) {
        return parentPublicId == null ? null : findActive(parentPublicId);
    }

    private void ensureSlugAvailable(String slug, Long currentId) {
        boolean duplicate = currentId == null
                ? categoryRepository.existsBySlugAndDeletedFalse(slug)
                : categoryRepository.existsBySlugAndDeletedFalseAndIdNot(slug, currentId);
        if (duplicate) {
            throw new DuplicateResourceException("Category slug is already in use.");
        }
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getPublicId(),
                category.getParent() == null ? null : category.getParent().getPublicId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getStatus(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
