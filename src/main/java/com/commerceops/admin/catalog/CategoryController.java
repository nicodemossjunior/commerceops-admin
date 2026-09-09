package com.commerceops.admin.catalog;

import com.commerceops.admin.catalog.dto.CategoryRequest;
import com.commerceops.admin.catalog.dto.CategoryResponse;
import com.commerceops.admin.catalog.service.CategoryService;
import com.commerceops.admin.common.pagination.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Catalog category and subcategory management")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private static final String READ_ROLES = "hasAnyRole('ADMIN', 'MANAGER', 'CATALOG', 'READ_ONLY', 'SUPPORT')";

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List categories", description = "Returns non-deleted categories using pagination.")
    @PreAuthorize(READ_ROLES)
    public PageResponse<CategoryResponse> list(Pageable pageable) {
        return categoryService.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CATALOG')")
    @Operation(summary = "Create a category")
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get a category by public ID")
    @PreAuthorize(READ_ROLES)
    public CategoryResponse get(@PathVariable UUID publicId) {
        return categoryService.get(publicId);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CATALOG')")
    @Operation(summary = "Update a category")
    public CategoryResponse update(
            @PathVariable UUID publicId,
            @Valid @RequestBody CategoryRequest request
    ) {
        return categoryService.update(publicId, request);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CATALOG')")
    @Operation(summary = "Soft delete a category")
    public void delete(@PathVariable UUID publicId) {
        categoryService.delete(publicId);
    }
}
