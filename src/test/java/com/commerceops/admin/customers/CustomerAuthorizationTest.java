package com.commerceops.admin.customers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.customers.service.CustomerNoteService;
import com.commerceops.admin.customers.service.CustomerService;
import java.util.List;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CustomerNoteService customerNoteService;

    @Test
    @WithMockUser(roles = "CATALOG")
    void catalogCanReadButCannotWriteCustomers() throws Exception {
        when(customerService.list(any(), any())).thenReturn(emptyPage());
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    void supportCanManageNotesButCannotDeleteCustomer() throws Exception {
        mockMvc.perform(post("/api/customers/{publicId}/notes", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Follow up requested.\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/customers/{publicId}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void readOnlyCannotSeeInternalNotes() throws Exception {
        mockMvc.perform(get("/api/customers/{publicId}/notes", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    private PageResponse<com.commerceops.admin.customers.dto.CustomerResponse> emptyPage() {
        return new PageResponse<>(List.of(), 0, 20, 0, 0, true, true);
    }

    private String validCustomerJson() {
        return """
                {"name":"Alice","email":"alice@example.com","status":"ACTIVE"}
                """;
    }
}
