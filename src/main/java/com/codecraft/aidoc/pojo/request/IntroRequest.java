package com.codecraft.aidoc.pojo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Collects user onboarding survey responses tied to a generated documentation sample.
 */
@Data
public class IntroRequest {

    @NotBlank(message = "反馈标识不能为空")
    private String id;

    @NotBlank(message = "用途不能为空")
    private String purpose;
}
