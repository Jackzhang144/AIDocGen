package com.codecraft.aidoc.security;

import com.codecraft.aidoc.enums.UserRole;
import com.codecraft.aidoc.pojo.entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Lightweight authenticated user representation used across the application.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String email;
    private final UserRole role;
    private final Integer apiQuota;

    public UserPrincipal(UserEntity entity) {
        this.id = entity.getId();
        this.username = entity.getUsername();
        this.password = entity.getPasswordHash();
        this.email = entity.getEmail();
        this.role = entity.getRole();
        this.apiQuota = entity.getApiQuota();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
