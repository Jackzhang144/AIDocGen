package com.codecraft.aidoc.security;

import com.codecraft.aidoc.enums.ErrorCode;
import com.codecraft.aidoc.exception.BusinessException;
import com.codecraft.aidoc.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that authenticates public API requests using the API-KEY header which is hashed and
 * looked up against the persisted API key catalogue.
 */
@Slf4j
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "API-KEY";

    private final ApiKeyService apiKeyService;
    private final RequestMatcher requestMatcher;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
        this.requestMatcher = new AntPathRequestMatcher("/api/v1/**");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !requestMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String rawKey = request.getHeader(API_KEY_HEADER);
        if (rawKey == null || rawKey.isBlank()) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "未提供 API-KEY 请求头");
        }

        final String sanitized = rawKey.trim();
        apiKeyService.findByRawKey(sanitized).ifPresentOrElse(apiKey -> {
            final ApiKeyAuthenticationToken authentication = new ApiKeyAuthenticationToken(apiKey.getHashedKey(), true, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }, () -> {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "API-KEY 无效");
        });

        filterChain.doFilter(request, response);
    }
}
