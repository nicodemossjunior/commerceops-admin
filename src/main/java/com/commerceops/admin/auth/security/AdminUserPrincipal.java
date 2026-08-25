package com.commerceops.admin.auth.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AdminUserPrincipal(
        Long id,
        UUID publicId,
        String email,
        List<String> roles
) {

    public Collection<? extends GrantedAuthority> authorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
