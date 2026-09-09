package com.commerceops.admin.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.orders.service.OrderService;
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
class OrderAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void readOnlyCanListButCannotChangeStatus() throws Exception {
        when(orderService.list(any(), any())).thenReturn(emptyPage());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/orders/{publicId}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"PAID\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CATALOG")
    void catalogCanReadButCannotCancelOrders() throws Exception {
        when(orderService.list(any(), any())).thenReturn(emptyPage());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/{publicId}/cancel", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Requested.\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    void supportCanReadButCannotRefundOrders() throws Exception {
        when(orderService.list(any(), any())).thenReturn(emptyPage());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/{publicId}/refund", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Requested.\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanPerformOperationalActions() throws Exception {
        UUID publicId = UUID.randomUUID();

        mockMvc.perform(patch("/api/orders/{publicId}/status", publicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"PAID\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/{publicId}/cancel", publicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Requested.\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/{publicId}/refund", publicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Approved.\"}"))
                .andExpect(status().isOk());
    }

    private PageResponse<com.commerceops.admin.orders.dto.OrderSummaryResponse> emptyPage() {
        return new PageResponse<>(List.of(), 0, 20, 0, 0, true, true);
    }
}
