package com.commerceops.admin.auth;

import com.commerceops.admin.auth.dto.AuthUserResponse;
import com.commerceops.admin.auth.dto.LoginRequest;
import com.commerceops.admin.auth.dto.LoginResponse;
import com.commerceops.admin.auth.security.AdminUserPrincipal;
import com.commerceops.admin.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Administrative authentication and current-user identity")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate an administrative user",
            description = "Validates an enabled administrator's credentials and returns a JWT Bearer access token. Public endpoint."
    )
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get the authenticated administrative user",
            description = "Returns the public identity and roles represented by the current JWT. Requires an authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AuthUserResponse me(@AuthenticationPrincipal AdminUserPrincipal principal) {
        return authService.me(principal.id());
    }
}
