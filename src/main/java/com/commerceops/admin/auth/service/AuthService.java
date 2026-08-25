package com.commerceops.admin.auth.service;

import com.commerceops.admin.auth.dto.AuthUserResponse;
import com.commerceops.admin.auth.dto.LoginRequest;
import com.commerceops.admin.auth.dto.LoginResponse;
import com.commerceops.admin.auth.model.AdminUser;
import com.commerceops.admin.auth.repository.AdminUserRepository;
import com.commerceops.admin.auth.security.JwtService;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String BEARER = "Bearer";

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        AdminUser user = adminUserRepository.findByEmailIgnoreCase(request.email())
                .filter(AdminUser::canAuthenticate)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(AuthenticationFailedException::new);

        user.recordLogin();
        String accessToken = jwtService.generateToken(user);

        return new LoginResponse(accessToken, BEARER, jwtService.accessTokenTtlSeconds(), AuthMapper.toResponse(user));
    }

    @Transactional(readOnly = true)
    public AuthUserResponse me(Long userId) {
        AdminUser user = adminUserRepository.findById(userId)
                .filter(candidate -> !candidate.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found."));

        return AuthMapper.toResponse(user);
    }
}
