package com.commerceops.admin.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.catalog.model.Category;
import com.commerceops.admin.catalog.model.CategoryStatus;
import com.commerceops.admin.catalog.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createsCategory() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Electronics",
                                  "slug": "electronics",
                                  "description": "Electronic products",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.slug").value("electronics"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updatesCategory() throws Exception {
        Category category = categoryRepository.saveAndFlush(
                new Category("Electronics", "electronics", null, CategoryStatus.ACTIVE, null)
        );

        mockMvc.perform(put("/api/categories/{publicId}", category.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Consumer Electronics",
                                  "slug": "consumer-electronics",
                                  "description": "Updated category",
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(category.getPublicId().toString()))
                .andExpect(jsonPath("$.name").value("Consumer Electronics"))
                .andExpect(jsonPath("$.slug").value("consumer-electronics"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void listsCategoriesWithPagination() throws Exception {
        categoryRepository.saveAndFlush(
                new Category("Electronics", "electronics", null, CategoryStatus.ACTIVE, null)
        );

        mockMvc.perform(get("/api/categories").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Electronics"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsDuplicateActiveSlug() throws Exception {
        categoryRepository.saveAndFlush(
                new Category("Electronics", "electronics", null, CategoryStatus.ACTIVE, null)
        );

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Other Electronics",
                                  "slug": "electronics",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }
}
