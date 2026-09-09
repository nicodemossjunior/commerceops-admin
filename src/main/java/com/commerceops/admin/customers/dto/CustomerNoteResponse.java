package com.commerceops.admin.customers.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerNoteResponse(
        UUID publicId,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
