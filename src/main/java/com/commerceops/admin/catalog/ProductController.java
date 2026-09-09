package com.commerceops.admin.catalog;

import com.commerceops.admin.catalog.dto.ProductFilter;
import com.commerceops.admin.catalog.dto.ProductRequest;
import com.commerceops.admin.catalog.dto.ProductResponse;
import com.commerceops.admin.catalog.model.ProductStatus;
import com.commerceops.admin.catalog.service.ProductService;
import com.commerceops.admin.common.pagination.PageResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public PageResponse<ProductResponse> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean lowStock,
            Pageable pageable
    ) {
        return productService.list(
                new ProductFilter(categoryId, status, sku, name, minPrice, maxPrice, lowStock),
                pageable
        );
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

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CATALOG')")
    public void delete(@PathVariable UUID publicId) {
        productService.delete(publicId);
    }
}
