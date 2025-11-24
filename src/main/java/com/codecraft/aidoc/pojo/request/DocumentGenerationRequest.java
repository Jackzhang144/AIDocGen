package com.codecraft.aidoc.pojo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for synchronous documentation generation requests.
 */
@Data
public class DocumentGenerationRequest {

    /**
     * Raw source code snippet provided by the client.
     */
    @NotBlank(message = "代码内容不能为空")
    private String code;

    /**
     * Language identifier used to tailor the generator output.
     */
    @NotBlank(message = "语言标识不能为空")
    private String language;

    /**
     * Optional flag instructing the backend to wrap the documentation using comment markers.
     */
    private Boolean commented = Boolean.FALSE;

    /**
     * Preferred documentation format; leave blank to auto-detect.
     */
    private String format;

    /**
     * Optional contextual block used primarily when the snippet is part of a larger file.
     */
    private String context;

    /**
     * Width hint used when wrapping comment blocks.
     */
    private Integer width;

    /**
     * 生成模式：text（仅注释文本）、inserted（带注释的完整代码）。
     */
    private String mode;

    /**
     * 质量档位：fast/balanced/deep。
     */
    private String quality;

    /**
     * 行内注释比例（0-1），用于提示模型控制行级注释密度。
     */
    private Double lineCommentRatio;
}
