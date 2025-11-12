package com.codecraft.aidoc.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.codecraft.aidoc.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads JWT tokens from the Authorization header and populates the Spring Security context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        jwtService.parse(token)
                .flatMap(this::buildPrincipal)
                .ifPresent(principal -> {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                });
        filterChain.doFilter(request, response);
    }

    private java.util.Optional<UserPrincipal> buildPrincipal(DecodedJWT jwt) {
        String subject = jwt.getSubject();
        if (!StringUtils.hasText(subject)) {
            return java.util.Optional.empty();
        }
        try {
            Long userId = Long.valueOf(subject);
            return userService.findById(userId).map(UserPrincipal::new);
        } catch (NumberFormatException ex) {
            log.warn("[AIDocGen] 无效的 JWT subject: {}", subject);
            return java.util.Optional.empty();
        }
    }
}
