package com.commerceops.admin.catalog;

import com.commerceops.admin.catalog.dto.CategoryRequest;
import com.commerceops.admin.catalog.dto.CategoryResponse;
import com.commerceops.admin.catalog.service.CategoryService;
import com.commerceops.admin.common.pagination.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public PageResponse<CategoryResponse> list(Pageable pageable) {
        return categoryService.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CATALOG')")
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @GetMapping("/{publicId}")
    public CategoryResponse get(@PathVariable UUID publicId) {
        return categoryService.get(publicId);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CATALOG')")
    public CategoryResponse update(
            @PathVariable UUID publicId,
            @Valid @RequestBody CategoryRequest request
    ) {
        return categoryService.update(publicId, request);
    }
}
