package com.codecraft.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Map;

public class JwtUtilStatic {
    private static Key key;
    private static long expirationMs;

    public static void init(String secret, long expireMs) {
        key = Keys.hmacShaKeyFor(secret.getBytes());
        expirationMs = expireMs;
    }

    public static String parseUsername(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return c.getSubject();
    }

    public static Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public static String generate(String username, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new java.util.Date(now))
                .setExpiration(new java.util.Date(now + expirationMs))
                .signWith(key)
                .compact();
    }
}
