package com.codecraft.documentationgenerator.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置类
 * <p>
 * 配置Spring Security，包括密码编码器、认证管理器和过滤器链
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /**
     * 密码编码器Bean
     * <p>
     * 使用BCrypt算法对密码进行加密
     *
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Initializing BCrypt password encoder");
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器Bean
     * <p>
     * 获取Spring Security的认证管理器
     *
     * @param config 认证配置
     * @return AuthenticationManager 认证管理器实例
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("Initializing authentication manager");
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤器链配置
     * <p>
     * 配置HTTP安全策略，包括CSRF禁用、请求授权规则和JWT过滤器
     *
     * @param http HttpSecurity对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}