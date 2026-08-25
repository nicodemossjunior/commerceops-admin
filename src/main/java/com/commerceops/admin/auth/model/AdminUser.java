package com.commerceops.admin.auth.model;

import com.commerceops.admin.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "admin_user")
public class AdminUser extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdminUserStatus status = AdminUserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "admin_user_role",
            joinColumns = @JoinColumn(name = "admin_user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new LinkedHashSet<>();

    protected AdminUser() {
    }

    public AdminUser(String name, String email, String passwordHash, Set<Role> roles) {
        this.name = name;
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.roles = new LinkedHashSet<>(roles);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AdminUserStatus getStatus() {
        return status;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public boolean canAuthenticate() {
        return status == AdminUserStatus.ACTIVE && !isDeleted();
    }

    public void disable() {
        status = AdminUserStatus.DISABLED;
    }

    public void recordLogin() {
        lastLoginAt = Instant.now();
    }
}
