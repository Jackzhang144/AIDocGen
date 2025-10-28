package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.Team;
import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.service.TeamServiceInterface;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 用户接口（Auth0、Stripe 相关能力）
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserServiceInterface userService;
    private final TeamServiceInterface teamService;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String auth0Issuer;
    private final String auth0ClientId;
    private final String auth0ClientSecret;

    public UserController(UserServiceInterface userService,
                          TeamServiceInterface teamService,
                          @Value("${AUTH0_ISSUER_BASE_URL:}") String auth0Issuer,
                          @Value("${AUTH0_CLIENT_ID:}") String auth0ClientId,
                          @Value("${AUTH0_CLIENT_SECRET:}") String auth0ClientSecret) {
        this.userService = userService;
        this.teamService = teamService;
        this.auth0Issuer = auth0Issuer;
        this.auth0ClientId = auth0ClientId;
        this.auth0ClientSecret = auth0ClientSecret;
    }

    @PostMapping("/code")
    public ResponseEntity<UserStatusResponse> exchangeCode(@RequestBody AuthCodeRequest request) {
        log.info("Received Auth0 code exchange for userUid={} (scheme={})", request.getUserId(), request.getUriScheme());
        ensureAuth0Configured();

        TokenResponse tokens = fetchTokensFromAuth0(request);
        AuthInfo authInfo = fetchAuthInfo(tokens.getAccessToken());

        if (authInfo.getEmail() == null) {
            throw new BusinessException("Invalid user token");
        }

        User user = upsertUser(request.getUserId(), authInfo, tokens.getRefreshToken());
        boolean isUpgraded = "premium".equalsIgnoreCase(user.getPlan());

        log.info("Auth0 exchange completed for {} (plan={})", maskEmail(authInfo.getEmail()), user.getPlan());

        UserStatusResponse response = new UserStatusResponse();
        response.setEmail(authInfo.getEmail());
        response.setUpgraded(isUpgraded);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkout")
    public ResponseEntity<Void> redirectCheckout(@RequestParam String email, @RequestParam(required = false) String scheme) {
        String redirect = "https://mintlify.com/pricing";
        log.info("Redirecting {} to checkout (scheme={})", email, scheme);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(redirect))
                .build();
    }

    @GetMapping("/portal")
    public ResponseEntity<Void> redirectPortal(@RequestParam String email, @RequestParam(required = false) String scheme) {
        String redirect = "https://mintlify.com/account";
        log.info("Redirecting {} to customer portal (scheme={})", email, scheme);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(redirect))
                .build();
    }

    @PostMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus(@RequestBody(required = false) Map<String, String> body) {
        String email = body == null ? null : body.get("email");
        if (email == null || email.isEmpty()) {
            log.info("Status check with missing email; returning unauthenticated");
            return ResponseEntity.ok(Map.of("status", "unauthenticated"));
        }

        User user = userService.findByEmailOrNull(email);
        if (user == null) {
            log.info("Status check for {} => unaccounted", maskEmail(email));
            return ResponseEntity.ok(Map.of("status", "unaccounted"));
        }

        if (user.getPlan() != null && "premium".equalsIgnoreCase(user.getPlan())) {
            log.info("Status check for {} => team (premium)", maskEmail(email));
            return ResponseEntity.ok(Map.of("status", "team"));
        }

        Team team = teamService.findByMember(email);
        if (team != null) {
            log.info("Status check for {} => member of team {}", maskEmail(email), team.getAdmin());
            return ResponseEntity.ok(Map.of("status", "member"));
        }

        log.info("Status check for {} => community", maskEmail(email));
        return ResponseEntity.ok(Map.of("status", "community"));
    }

    private void ensureAuth0Configured() {
        if (auth0Issuer == null || auth0Issuer.isEmpty()
                || auth0ClientId == null || auth0ClientId.isEmpty()
                || auth0ClientSecret == null || auth0ClientSecret.isEmpty()) {
            throw new BusinessException("Auth0 credentials are not configured");
        }
    }

    private TokenResponse fetchTokensFromAuth0(AuthCodeRequest request) {
        String redirectScheme = request.getUriScheme() == null ? "vscode" : request.getUriScheme();

        java.util.Map<String, Object> payload = Map.of(
                "grant_type", "authorization_code",
                "client_id", auth0ClientId,
                "client_secret", auth0ClientSecret,
                "code", request.getCode(),
                "redirect_uri", "https://mintlify.com/start/" + redirectScheme
        );

        TokenResponse response = restTemplate.postForObject(
                auth0Issuer + "/oauth/token",
                new org.springframework.http.HttpEntity<>(payload, jsonHeaders()),
                TokenResponse.class);

        if (response == null || response.getAccessToken() == null) {
            throw new BusinessException("Failed to exchange code for tokens");
        }
        log.debug("Received token response from Auth0 for scheme {}", redirectScheme);
        return response;
    }

    private AuthInfo fetchAuthInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
        var response = restTemplate.exchange(auth0Issuer + "/userinfo",
                org.springframework.http.HttpMethod.GET,
                entity,
                AuthInfo.class);
        if (!response.hasBody()) {
            throw new BusinessException("Failed to fetch user info");
        }
        log.debug("Fetched user info from Auth0 with status {}", response.getStatusCode());
        return response.getBody();
    }

    private User upsertUser(String userUid, AuthInfo authInfo, String refreshToken) {
        User existing = userService.findByEmailOrNull(authInfo.getEmail());
        if (existing == null) {
            User user = new User();
            user.setUserUid(userUid);
            user.setEmail(authInfo.getEmail());
            user.setName(authInfo.getName());
            user.setGivenName(authInfo.getGivenName());
            user.setFamilyName(authInfo.getFamilyName());
            user.setPicture(authInfo.getPicture());
            user.setRefreshToken(refreshToken);
            user.setCreatedAt(LocalDateTime.now());
            userService.createUser(user);
            log.info("Created new user record for {} via Auth0", maskEmail(authInfo.getEmail()));
            return user;
        }

        existing.setUserUid(userUid);
        existing.setRefreshToken(refreshToken);
        existing.setName(authInfo.getName());
        existing.setGivenName(authInfo.getGivenName());
        existing.setFamilyName(authInfo.getFamilyName());
        existing.setPicture(authInfo.getPicture());
        userService.updateProfile(existing);
        log.debug("Updated existing user profile for {} via Auth0", maskEmail(authInfo.getEmail()));
        return existing;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "<empty>";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + (atIndex == -1 ? "" : email.substring(atIndex));
        }
        return email.substring(0, Math.min(2, atIndex)) + "***" + email.substring(atIndex);
    }

    @Data
    public static class AuthCodeRequest {
        private String code;
        private String userId;
        private String uriScheme;
    }

    @Data
    public static class TokenResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("access_token")
        private String accessToken;
        @com.fasterxml.jackson.annotation.JsonProperty("refresh_token")
        private String refreshToken;
    }

    @Data
    public static class AuthInfo {
        private String email;
        @com.fasterxml.jackson.annotation.JsonProperty("given_name")
        private String givenName;
        @com.fasterxml.jackson.annotation.JsonProperty("family_name")
        private String familyName;
        private String name;
        private String picture;
    }

    @Data
    public static class UserStatusResponse {
        private String email;
        @com.fasterxml.jackson.annotation.JsonProperty("isUpgraded")
        private boolean upgraded;
    }
}
