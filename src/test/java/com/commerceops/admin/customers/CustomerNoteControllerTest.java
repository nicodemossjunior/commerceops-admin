package com.commerceops.admin.customers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.auth.model.AdminUser;
import com.commerceops.admin.auth.model.Role;
import com.commerceops.admin.auth.repository.AdminUserRepository;
import com.commerceops.admin.auth.repository.RoleRepository;
import com.commerceops.admin.common.security.CurrentUser;
import com.commerceops.admin.common.security.CurrentUserProvider;
import com.commerceops.admin.customers.model.Customer;
import com.commerceops.admin.customers.model.CustomerNote;
import com.commerceops.admin.customers.model.CustomerStatus;
import com.commerceops.admin.customers.repository.CustomerNoteRepository;
import com.commerceops.admin.customers.repository.CustomerRepository;
import java.util.Set;
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
class CustomerNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerNoteRepository customerNoteRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    private Customer customer;
    private AdminUser supportUser;

    @BeforeEach
    void setUp() {
        Role supportRole = roleRepository.findByName("SUPPORT").orElseThrow();
        supportUser = adminUserRepository.saveAndFlush(
                new AdminUser("Support User", "support-notes@example.com", "unused-password-hash", Set.of(supportRole))
        );
        customer = customerRepository.saveAndFlush(
                new Customer("Alice Smith", "alice@example.com", null, null, CustomerStatus.ACTIVE)
        );
        when(currentUserProvider.currentUser()).thenReturn(new CurrentUser(
                supportUser.getId(),
                supportUser.getPublicId(),
                supportUser.getEmail(),
                Set.of("SUPPORT")
        ));
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    void createsAndListsCustomerNote() throws Exception {
        mockMvc.perform(post("/api/customers/{publicId}/notes", customer.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note": "Customer requested a callback."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.note").value("Customer requested a callback."))
                .andExpect(jsonPath("$.createdBy").doesNotExist());

        mockMvc.perform(get("/api/customers/{publicId}/notes", customer.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].note").value("Customer requested a callback."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void softDeletesCustomerNote() throws Exception {
        CustomerNote note = customerNoteRepository.saveAndFlush(
                new CustomerNote(customer, "Sensitive internal note", supportUser.getId())
        );

        mockMvc.perform(delete(
                        "/api/customers/{publicId}/notes/{notePublicId}",
                        customer.getPublicId(),
                        note.getPublicId()
                ))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{publicId}/notes", customer.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        CustomerNote deleted = customerNoteRepository.findById(note.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(deleted.isDeleted()).isTrue();
        org.assertj.core.api.Assertions.assertThat(deleted.getDeletedBy()).isEqualTo(supportUser.getId());
    }
}
