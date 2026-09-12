package com.commerceops.admin.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorrelationIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generatesCorrelationIdsAndReturnsThemInResponseHeaders() throws Exception {
        var result = mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists(CorrelationContext.REQUEST_ID_HEADER))
                .andExpect(header().exists(CorrelationContext.TRACE_ID_HEADER))
                .andReturn();

        String requestId = result.getResponse().getHeader(CorrelationContext.REQUEST_ID_HEADER);
        String traceId = result.getResponse().getHeader(CorrelationContext.TRACE_ID_HEADER);
        assertThat(UUID.fromString(requestId)).isNotNull();
        assertThat(UUID.fromString(traceId)).isNotNull();
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void preservesValidIdsAndIncludesTraceIdInSecurityErrors() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header(CorrelationContext.REQUEST_ID_HEADER, "request-client-123")
                        .header(CorrelationContext.TRACE_ID_HEADER, "trace-client-456"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(CorrelationContext.REQUEST_ID_HEADER, "request-client-123"))
                .andExpect(header().string(CorrelationContext.TRACE_ID_HEADER, "trace-client-456"))
                .andExpect(jsonPath("$.traceId").value("trace-client-456"));
    }

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void generatedTraceIdAppearsInApplicationErrors() throws Exception {
        var result = mockMvc.perform(get("/api/products/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists(CorrelationContext.TRACE_ID_HEADER))
                .andReturn();

        String traceId = result.getResponse().getHeader(CorrelationContext.TRACE_ID_HEADER);
        assertThat(result.getResponse().getContentAsString()).contains("\"traceId\":\"" + traceId + "\"");
    }
}
