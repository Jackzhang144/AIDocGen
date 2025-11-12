package com.codecraft.aidoc.pojo.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Admin payload for updating user metadata.
 */
@Data
public class UpdateUserRequest {

    private String email;

    private String role;

    @Min(value = -1, message = "配额必须 >= -1")
    private Integer apiQuota;
}
