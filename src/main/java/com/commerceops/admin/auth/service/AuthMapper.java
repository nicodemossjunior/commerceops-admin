package com.commerceops.admin.auth.service;

import com.commerceops.admin.auth.dto.AuthUserResponse;
import com.commerceops.admin.auth.model.AdminUser;
import com.commerceops.admin.auth.model.Role;
import java.util.Comparator;

public final class AuthMapper {

    private AuthMapper() {
    }

    public static AuthUserResponse toResponse(AdminUser user) {
        return new AuthUserResponse(
                user.getPublicId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .sorted(Comparator.naturalOrder())
                        .toList()
        );
    }
}
