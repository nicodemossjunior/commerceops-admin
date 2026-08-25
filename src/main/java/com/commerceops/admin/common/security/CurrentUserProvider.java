package com.commerceops.admin.common.security;

import com.commerceops.admin.auth.security.AdminUserPrincipal;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminUserPrincipal principal)) {
            throw new ResourceNotFoundException("Authenticated user was not found.");
        }

        return new CurrentUser(principal.id(), principal.publicId(), principal.email(), java.util.Set.copyOf(principal.roles()));
    }
}
