package com.codecraft.documentationgenerator.config.security;

import com.codecraft.documentationgenerator.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT请求过滤器
 * <p>
 * 拦截每个HTTP请求，验证JWT令牌并设置认证信息
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 过滤HTTP请求，验证JWT令牌
     * <p>
     * 从请求头中提取JWT令牌，验证其有效性，并设置Spring Security上下文
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param chain    过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException      IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        log.debug("Processing JWT token validation for request: {} {}", request.getMethod(), request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwtToken = null;

        // 从Authorization头部提取JWT令牌
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(jwtToken);
                log.debug("Extracted email from JWT token: {}", email);
            } catch (Exception e) {
                log.warn("Failed to extract email from JWT token: {}", e.getMessage());
            }
        }

        // 如果提取到邮箱且安全上下文中没有认证信息，则验证令牌
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwtToken, email)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Successfully authenticated user: {}", email);
            } else {
                log.warn("Invalid JWT token for user: {}", email);
            }
        }

        chain.doFilter(request, response);
    }
}