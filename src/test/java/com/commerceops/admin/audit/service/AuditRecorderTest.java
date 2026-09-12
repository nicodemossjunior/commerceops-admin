package com.commerceops.admin.audit.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.common.security.CurrentUser;
import com.commerceops.admin.common.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuditRecorderTest {

    @Test
    void auditFailureDoesNotBreakTheBusinessOperation() {
        AuditWriteService writeService = mock(AuditWriteService.class);
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        when(currentUserProvider.currentUser()).thenReturn(
                new CurrentUser(1L, UUID.randomUUID(), "admin@example.com", Set.of("ADMIN"))
        );
        doThrow(new IllegalStateException("Database unavailable")).when(writeService).write(any());
        AuditRecorder recorder = new AuditRecorder(
                writeService,
                new AuditMetadataRedactor(new ObjectMapper()),
                new AuditRequestContext(),
                currentUserProvider
        );

        assertThatCode(() -> recorder.record(
                AuditAction.PRODUCT_UPDATED,
                "PRODUCT",
                UUID.randomUUID(),
                Map.of("field", "price")
        )).doesNotThrowAnyException();
    }
}
