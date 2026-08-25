package com.commerceops.admin.auth.dto;

import java.util.List;
import java.util.UUID;

public record AuthUserResponse(
        UUID publicId,
        String name,
        String email,
        List<String> roles
) {
}
