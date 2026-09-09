package com.commerceops.admin.customers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.common.security.CurrentUser;
import com.commerceops.admin.common.security.CurrentUserProvider;
import com.commerceops.admin.customers.model.Customer;
import com.commerceops.admin.customers.model.CustomerStatus;
import com.commerceops.admin.customers.repository.CustomerRepository;
import java.util.Set;
import java.util.UUID;
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
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createsCustomer() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Alice Smith", "Alice@Example.com", "+55 85 99999-9999", "ACTIVE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updatesCustomer() throws Exception {
        Customer customer = saveCustomer("Alice Smith", "alice@example.com", CustomerStatus.ACTIVE);

        mockMvc.perform(put("/api/customers/{publicId}", customer.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Alice Johnson", "alice.j@example.com", "+55 85 98888-8888", "BLOCKED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(customer.getPublicId().toString()))
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void filtersCustomerListing() throws Exception {
        saveCustomer("Alice Smith", "alice@example.com", CustomerStatus.ACTIVE);
        saveCustomer("Bob Jones", "bob@example.com", CustomerStatus.BLOCKED);

        mockMvc.perform(get("/api/customers")
                        .param("name", "alice")
                        .param("email", "example.com")
                        .param("phone", "99999")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Alice Smith"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsDuplicateEmail() throws Exception {
        saveCustomer("Alice Smith", "alice@example.com", CustomerStatus.ACTIVE);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Other Alice", "ALICE@example.com", null, "ACTIVE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsInvalidContactData() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Alice", "invalid-email", "abc", "ACTIVE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void softDeletesCustomer() throws Exception {
        Customer customer = saveCustomer("Alice Smith", "alice@example.com", CustomerStatus.ACTIVE);
        when(currentUserProvider.currentUser())
                .thenReturn(new CurrentUser(42L, UUID.randomUUID(), "manager@example.com", Set.of("MANAGER")));

        mockMvc.perform(delete("/api/customers/{publicId}", customer.getPublicId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{publicId}", customer.getPublicId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        Customer deleted = customerRepository.findById(customer.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(deleted.isDeleted()).isTrue();
        org.assertj.core.api.Assertions.assertThat(deleted.getDeletedBy()).isEqualTo(42L);
    }

    private Customer saveCustomer(String name, String email, CustomerStatus status) {
        return customerRepository.saveAndFlush(
                new Customer(name, email, "+55 85 99999-9999", "DOC-001", status)
        );
    }

    private String customerJson(String name, String email, String phone, String status) {
        String emailValue = email == null ? "null" : "\"" + email + "\"";
        String phoneValue = phone == null ? "null" : "\"" + phone + "\"";
        return """
                {
                  "name": "%s",
                  "email": %s,
                  "phone": %s,
                  "document": "DOC-001",
                  "status": "%s"
                }
                """.formatted(name, emailValue, phoneValue, status);
    }
}
