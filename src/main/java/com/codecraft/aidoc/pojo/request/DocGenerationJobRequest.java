package com.codecraft.aidoc.pojo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Shared payload for both selection and non-selection documentation jobs.
 */
@Data
public class DocGenerationJobRequest {

    private String code;

    private String context;

    @NotBlank(message = "语言标识不能为空")
    private String languageId;

    private String email;

    private Boolean commented;

    private String userId;

    private String docStyle;

    private String fileName;

    private Boolean isSelection;

    private String location;

    private Integer line;

    private Integer width;

    private String mode;

    private String source;

    /**
     * 质量档位：fast/balanced/deep。
     */
    private String quality;

    /**
     * 行内注释比例（0-1）。
     */
    private Double lineCommentRatio;
}
