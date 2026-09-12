package com.commerceops.admin.auth.service;

import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.audit.service.AuditRecorder;
import com.commerceops.admin.auth.dto.AuthUserResponse;
import com.commerceops.admin.auth.dto.LoginRequest;
import com.commerceops.admin.auth.dto.LoginResponse;
import com.commerceops.admin.auth.model.AdminUser;
import com.commerceops.admin.auth.repository.AdminUserRepository;
import com.commerceops.admin.auth.security.JwtService;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import com.commerceops.admin.observability.CommerceOpsMetrics;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String BEARER = "Bearer";

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditRecorder auditRecorder;
    private final CommerceOpsMetrics metrics;

    public AuthService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditRecorder auditRecorder,
            CommerceOpsMetrics metrics
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditRecorder = auditRecorder;
        this.metrics = metrics;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        AdminUser user = adminUserRepository.findByEmailIgnoreCase(request.email()).orElse(null);
        if (user == null
                || !user.canAuthenticate()
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            metrics.recordLoginFailure();
            auditRecorder.recordAs(
                    user == null ? null : user.getId(),
                    request.email(),
                    AuditAction.AUTH_LOGIN_FAILURE,
                    "ADMIN_USER",
                    user == null ? null : user.getPublicId(),
                    Map.of("result", "INVALID_CREDENTIALS")
            );
            throw new AuthenticationFailedException();
        }

        user.recordLogin();
        String accessToken = jwtService.generateToken(user);
        metrics.recordLoginSuccess();
        auditRecorder.recordAs(
                user.getId(),
                user.getEmail(),
                AuditAction.AUTH_LOGIN_SUCCESS,
                "ADMIN_USER",
                user.getPublicId(),
                Map.of("result", "SUCCESS")
        );

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
