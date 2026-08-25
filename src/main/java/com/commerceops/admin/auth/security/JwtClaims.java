package com.commerceops.admin.auth.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JwtClaims(
        Long userId,
        UUID publicId,
        String email,
        List<String> roles,
        Instant expiresAt
) {
}
