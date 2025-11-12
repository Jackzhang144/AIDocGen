package com.codecraft.aidoc.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.codecraft.aidoc.config.JwtProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Minimal JWT helper for issuing and verifying stateless access tokens.
 */
@Component
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(Long userId, String username, String role) {
        long expiresAt = Instant.now().toEpochMilli() + jwtProperties.getExpirationMs();
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("username", username)
                .withClaim("role", role)
                .withExpiresAt(new Date(expiresAt))
                .sign(algorithm);
    }

    public Optional<DecodedJWT> parse(String token) {
        try {
            return Optional.ofNullable(verifier.verify(token));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
