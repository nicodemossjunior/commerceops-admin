package com.commerceops.admin.catalog;

import com.commerceops.admin.catalog.dto.ProductRequest;
import com.commerceops.admin.catalog.dto.ProductResponse;
import com.commerceops.admin.catalog.service.ProductService;
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
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public PageResponse<ProductResponse> list(Pageable pageable) {
        return productService.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CATALOG')")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping("/{publicId}")
    public ProductResponse get(@PathVariable UUID publicId) {
        return productService.get(publicId);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CATALOG')")
    public ProductResponse update(
            @PathVariable UUID publicId,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.update(publicId, request);
    }
}
