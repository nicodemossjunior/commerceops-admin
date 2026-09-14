package com.commerceops.admin.catalog;

import com.commerceops.admin.catalog.dto.ProductFilter;
import com.commerceops.admin.catalog.dto.ProductRequest;
import com.commerceops.admin.catalog.dto.ProductResponse;
import com.commerceops.admin.catalog.model.ProductStatus;
import com.commerceops.admin.catalog.service.ProductService;
import com.commerceops.admin.common.pagination.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Products", description = "Catalog product management and search")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private static final String READ_ROLES = "hasAnyRole('ADMIN', 'MANAGER', 'CATALOG', 'READ_ONLY', 'SUPPORT')";

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(
            summary = "List and filter products",
            description = "Returns non-deleted products. Low stock means 10 units or fewer."
    )
    @PreAuthorize(READ_ROLES)
    public PageResponse<ProductResponse> list(
            @Parameter(description = "Category public UUID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Product status") @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "Case-insensitive partial SKU") @RequestParam(required = false) String sku,
            @Parameter(description = "Case-insensitive partial product name") @RequestParam(required = false) String name,
            @Parameter(description = "Inclusive minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Inclusive maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "True for stock at or below 10; false for stock above 10")
            @RequestParam(required = false) Boolean lowStock,
            @ParameterObject Pageable pageable
    ) {
        return productService.list(
                new ProductFilter(categoryId, status, sku, name, minPrice, maxPrice, lowStock),
                pageable
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CATALOG')")
    @Operation(summary = "Create a product")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get a product by public ID")
    @PreAuthorize(READ_ROLES)
    public ProductResponse get(@PathVariable UUID publicId) {
        return productService.get(publicId);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CATALOG')")
    @Operation(summary = "Update a product")
    public ProductResponse update(
            @PathVariable UUID publicId,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.update(publicId, request);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CATALOG')")
    @Operation(summary = "Soft delete a product")
    public void delete(@PathVariable UUID publicId) {
        productService.delete(publicId);
    }
}
