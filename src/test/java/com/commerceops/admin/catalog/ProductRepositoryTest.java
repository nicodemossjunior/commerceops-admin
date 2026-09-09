package com.commerceops.admin.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerceops.admin.catalog.dto.ProductFilter;
import com.commerceops.admin.catalog.model.Category;
import com.commerceops.admin.catalog.model.CategoryStatus;
import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.catalog.model.ProductStatus;
import com.commerceops.admin.catalog.repository.CategoryRepository;
import com.commerceops.admin.catalog.repository.ProductRepository;
import com.commerceops.admin.catalog.repository.ProductSpecifications;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void combinesFiltersAndExcludesDeletedProducts() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Electronics", "electronics", null, CategoryStatus.ACTIVE, null)
        );
        save(category, "MOUSE-001", "Wireless Mouse", new BigDecimal("99.90"), 5, ProductStatus.ACTIVE, false);
        save(category, "MOUSE-002", "Deleted Mouse", new BigDecimal("79.90"), 4, ProductStatus.ACTIVE, true);
        save(category, "KEYBOARD-001", "Mechanical Keyboard", new BigDecimal("299.90"), 20, ProductStatus.ACTIVE, false);

        ProductFilter filter = new ProductFilter(
                category.getPublicId(),
                ProductStatus.ACTIVE,
                "mouse",
                "wireless",
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                true
        );

        Page<Product> result = productRepository.findAll(
                ProductSpecifications.withFilters(filter),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Product::getSku).containsExactly("MOUSE-001");
    }

    private void save(
            Category category,
            String sku,
            String name,
            BigDecimal price,
            int stock,
            ProductStatus status,
            boolean deleted
    ) {
        Product product = new Product(
                category,
                sku,
                name,
                name.toLowerCase(java.util.Locale.ROOT).replace(' ', '-'),
                null,
                price,
                null,
                stock,
                status
        );
        if (deleted) {
            product.markDeleted(1L);
        }
        productRepository.saveAndFlush(product);
    }
}
