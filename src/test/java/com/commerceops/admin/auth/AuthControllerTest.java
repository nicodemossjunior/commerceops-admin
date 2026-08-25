package com.commerceops.admin.auth;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceops.admin.auth.model.AdminUser;
import com.commerceops.admin.auth.model.Role;
import com.commerceops.admin.auth.repository.AdminUserRepository;
import com.commerceops.admin.auth.repository.RoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(AuthControllerTest.TestAuthorizationController.class)
class AuthControllerTest {

    private static final String PASSWORD = "correct-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role adminRole;
    private Role readOnlyRole;

    @BeforeEach
    void setUp() {
        adminUserRepository.deleteAll();
        adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        readOnlyRole = roleRepository.findByName("READ_ONLY").orElseThrow();
    }

    @Test
    void loginReturnsAccessTokenForValidCredentials() throws Exception {
        createUser("Admin User", "admin@example.com", PASSWORD, adminRole);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@example.com",
                                  "password": "correct-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.email").value("admin@example.com"))
                .andExpect(jsonPath("$.user.roles", containsInAnyOrder("ADMIN")))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void loginRejectsInvalidCredentialsWithoutRevealingUserExistence() throws Exception {
        createUser("Admin User", "admin@example.com", PASSWORD, adminRole);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void loginRejectsDisabledUsers() throws Exception {
        AdminUser user = createUser("Disabled User", "disabled@example.com", PASSWORD, adminRole);
        user.disable();
        adminUserRepository.saveAndFlush(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "disabled@example.com",
                                  "password": "correct-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    void loginRejectsDeletedUsers() throws Exception {
        AdminUser user = createUser("Deleted User", "deleted@example.com", PASSWORD, adminRole);
        user.markDeleted(1L);
        adminUserRepository.saveAndFlush(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "deleted@example.com",
                                  "password": "correct-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    void protectedEndpointRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    void protectedEndpointRejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    void protectedEndpointRejectsInsufficientRole() throws Exception {
        createUser("Read Only User", "readonly@example.com", PASSWORD, readOnlyRole);
        String accessToken = login("readonly@example.com", PASSWORD);

        mockMvc.perform(get("/test/authorization/admin-only")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void meReturnsAuthenticatedUserProfile() throws Exception {
        createUser("Admin User", "admin@example.com", PASSWORD, adminRole);
        String accessToken = login("admin@example.com", PASSWORD);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN")))
                .andExpect(jsonPath("$.*", not(containsInAnyOrder("passwordHash"))));
    }

    private AdminUser createUser(String name, String email, String password, Role role) {
        AdminUser user = new AdminUser(name, email, passwordEncoder.encode(password), Set.of(role));
        return adminUserRepository.saveAndFlush(user);
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("accessToken").asText();
    }

    @RestController
    @RequestMapping("/test/authorization")
    static class TestAuthorizationController {

        @GetMapping("/admin-only")
        @PreAuthorize("hasRole('ADMIN')")
        String adminOnly() {
            return "ok";
        }
    }
}
