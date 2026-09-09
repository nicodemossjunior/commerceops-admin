package com.commerceops.admin.catalog;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.catalog.model.Category;
import com.commerceops.admin.catalog.model.CategoryStatus;
import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.catalog.model.ProductStatus;
import com.commerceops.admin.catalog.repository.CategoryRepository;
import com.commerceops.admin.catalog.repository.ProductRepository;
import com.commerceops.admin.common.security.CurrentUser;
import com.commerceops.admin.common.security.CurrentUserProvider;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    private Category category;

    @BeforeEach
    void setUp() {
        category = categoryRepository.saveAndFlush(
                new Category("Electronics", "electronics", null, CategoryStatus.ACTIVE, null)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createsProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("sku-001", "Wireless Mouse", "wireless-mouse", "99.90", 15, "ACTIVE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.categoryPublicId").value(category.getPublicId().toString()))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.stockQuantity").value(15))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updatesProduct() throws Exception {
        Product product = saveProduct("SKU-001", "Wireless Mouse", ProductStatus.ACTIVE);

        mockMvc.perform(put("/api/products/{publicId}", product.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("sku-002", "Ergonomic Mouse", "ergonomic-mouse", "129.90", 9, "INACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(product.getPublicId().toString()))
                .andExpect(jsonPath("$.sku").value("SKU-002"))
                .andExpect(jsonPath("$.name").value("Ergonomic Mouse"))
                .andExpect(jsonPath("$.stockQuantity").value(9))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsDuplicateSku() throws Exception {
        saveProduct("SKU-001", "Wireless Mouse", ProductStatus.ACTIVE);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("sku-001", "Other Mouse", "other-mouse", "50.00", 2, "ACTIVE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsNegativePriceAndStock() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("sku-001", "Mouse", "mouse", "-1.00", -1, "ACTIVE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void listsProductsWithPagination() throws Exception {
        saveProduct("SKU-001", "Wireless Mouse", ProductStatus.ACTIVE);

        mockMvc.perform(get("/api/products").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void filtersProductsByCombinedCriteria() throws Exception {
        saveProduct("MOUSE-001", "Wireless Mouse", ProductStatus.ACTIVE, new BigDecimal("99.90"), 5);
        saveProduct("KEYBOARD-001", "Mechanical Keyboard", ProductStatus.ACTIVE, new BigDecimal("299.90"), 20);
        saveProduct("MOUSE-002", "Legacy Mouse", ProductStatus.INACTIVE, new BigDecimal("49.90"), 2);

        mockMvc.perform(get("/api/products")
                        .param("categoryId", category.getPublicId().toString())
                        .param("status", "ACTIVE")
                        .param("sku", "mouse")
                        .param("name", "wireless")
                        .param("minPrice", "50.00")
                        .param("maxPrice", "150.00")
                        .param("lowStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("MOUSE-001"));
    }

    @Test
    @WithMockUser(roles = "CATALOG")
    void softDeletesProduct() throws Exception {
        Product product = saveProduct("SKU-001", "Wireless Mouse", ProductStatus.ACTIVE);
        when(currentUserProvider.currentUser())
                .thenReturn(new CurrentUser(42L, UUID.randomUUID(), "catalog@example.com", Set.of("CATALOG")));

        mockMvc.perform(delete("/api/products/{publicId}", product.getPublicId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{publicId}", product.getPublicId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        Product deleted = productRepository.findById(product.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(deleted.isDeleted()).isTrue();
        org.assertj.core.api.Assertions.assertThat(deleted.getDeletedBy()).isEqualTo(42L);
    }

    private Product saveProduct(String sku, String name, ProductStatus status) {
        return saveProduct(sku, name, status, new BigDecimal("99.90"), 10);
    }

    private Product saveProduct(
            String sku,
            String name,
            ProductStatus status,
            BigDecimal price,
            int stockQuantity
    ) {
        return productRepository.saveAndFlush(new Product(
                category,
                sku,
                name,
                name.toLowerCase(java.util.Locale.ROOT).replace(' ', '-'),
                null,
                price,
                null,
                stockQuantity,
                status
        ));
    }

    private String productJson(String sku, String name, String slug, String price, int stock, String status) {
        return """
                {
                  "categoryPublicId": "%s",
                  "sku": "%s",
                  "name": "%s",
                  "slug": "%s",
                  "price": %s,
                  "stockQuantity": %d,
                  "status": "%s"
                }
                """.formatted(category.getPublicId(), sku, name, slug, price, stock, status);
    }
}
