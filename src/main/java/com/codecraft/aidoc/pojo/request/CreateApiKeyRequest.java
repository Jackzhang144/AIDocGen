package com.codecraft.aidoc.pojo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for admin API key provisioning.
 */
@Data
public class CreateApiKeyRequest {

    @NotBlank(message = "名不能为空")
    private String firstName;

    @NotBlank(message = "姓不能为空")
    private String lastName;

    @Email
    private String email;

    private String purpose;

    @NotBlank(message = "原始 API Key 不能为空")
    private String rawKey;
}
