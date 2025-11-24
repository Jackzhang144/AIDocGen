package com.codecraft.aidoc.gateway;

import com.codecraft.aidoc.enums.CommentFormat;
import com.codecraft.aidoc.enums.DocFormat;
import com.codecraft.aidoc.enums.LanguageId;
import com.codecraft.aidoc.pojo.dto.CodeSynopsis;
import lombok.Builder;
import lombok.Data;

/**
 * 描述一次模型调用所需的业务上下文，便于不同供应商按照统一结构取值。
 */
@Data
@Builder
public class ModelGatewayRequest {

    /**
     * 源代码片段。
     */
    private String code;

    /**
     * 附加的上下文信息，如整个文件内容。
     */
    private String context;

    /**
     * 识别出的语言。
     */
    private LanguageId languageId;

    /**
     * 选定的文档格式。
     */
    private DocFormat docFormat;

    /**
     * 推导出的注释格式。
     */
    private CommentFormat commentFormat;

    /**
     * 是否需要返回带注释的文档。
     */
    private boolean commented;

    /**
     * 代码结构摘要，供 Prompt 直接引用。
     */
    private CodeSynopsis synopsis;

    /**
     * 期望的最大行宽，用于提示模型控制长度。
     */
    private Integer width;

    /**
     * 质量档位（fast/balanced/deep），用于提示模型控制长度与细节。
     */
    private String quality;

    /**
     * 行内注释比例，提示模型在多大程度上添加行级注释。
     */
    private Double lineCommentRatio;
}
