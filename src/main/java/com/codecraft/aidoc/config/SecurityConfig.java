package com.codecraft.aidoc.config;

import com.codecraft.aidoc.security.ApiKeyAuthenticationFilter;
import com.codecraft.aidoc.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configures Spring Security to protect the public API using API keys and keep the rest of the endpoints
 * stateless. External documentation and actuator endpoints remain publicly accessible.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Builds the HTTP security pipeline with stateless sessions and the custom API key filter.
     *
     * @param http                        security builder
     * @param apiKeyAuthenticationFilter  custom filter that handles API-KEY authentication
     * @return configured security filter chain
     * @throws Exception propagated configuration errors
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers(
                                new AntPathRequestMatcher("/actuator/**"),
                                new AntPathRequestMatcher("/v3/api-docs/**"),
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/doc.html"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/auth/**")
                        ).permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .requestMatchers("/docs/**", "/admin/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Password encoder bean required by Spring Security. We do not manage password credentials in this service
     * therefore a no-op encoder suffices.
     *
     * @return no-op password encoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
