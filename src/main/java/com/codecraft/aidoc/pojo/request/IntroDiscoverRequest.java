package com.codecraft.aidoc.pojo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for tracking discovery channel during the onboarding flow.
 */
@Data
public class IntroDiscoverRequest {

    @NotBlank(message = "反馈标识不能为空")
    private String id;

    @NotBlank(message = "来源不能为空")
    private String source;
}
