package com.codecraft.aidoc.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight authentication token that carries the hashed API key identifier.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String credentials;

    public ApiKeyAuthenticationToken(String hashedKey, boolean authenticated, Collection<String> authorities) {
        super(authorities == null ? List.of() : authorities.stream().map(SimpleGrantedAuthority::new).toList());
        this.principal = hashedKey;
        this.credentials = hashedKey;
        setAuthenticated(authenticated);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(principal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ApiKeyAuthenticationToken that = (ApiKeyAuthenticationToken) obj;
        return Objects.equals(principal, that.principal);
    }
}
