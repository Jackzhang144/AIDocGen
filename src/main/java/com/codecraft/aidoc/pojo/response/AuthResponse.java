package com.codecraft.aidoc.pojo.response;

import com.codecraft.aidoc.enums.UserRole;
import lombok.Builder;
import lombok.Data;

/**
 * Response returned after registration or login containing JWT and basic profile data.
 */
@Data
@Builder
public class AuthResponse {

    private String token;
    private String username;
    private String email;
    private UserRole role;
    private Integer apiQuota;
}
