package com.commerceops.admin.common.security;

import java.util.Set;
import java.util.UUID;

public record CurrentUser(
        Long id,
        UUID publicId,
        String email,
        Set<String> roles
) {
}
