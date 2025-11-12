package com.codecraft.aidoc.pojo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload submitted when end-users provide feedback about generated documentation quality.
 */
@Data
public class FeedbackRequest {

    @NotBlank(message = "反馈标识不能为空")
    private String id;

    @NotNull(message = "反馈评分不能为空")
    private Integer feedback;
}
