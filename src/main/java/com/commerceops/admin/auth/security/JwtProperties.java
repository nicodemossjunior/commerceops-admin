package com.commerceops.admin.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commerceops.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenTtlSeconds
) {
}
