package com.codecraft.aidoc.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encapsulates the output of a documentation generation attempt.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DocGenerationResult {

    /**
     * Generated documentation string; null when generation failed.
     */
    private String documentation;

    /**
     * Optional preview snippet used by the desktop editor integrations.
     */
    private String preview;

    /**
     * Identifier used for follow-up feedback submissions.
     */
    private String feedbackId;

    /**
     * Indicates the preferred insertion position within the source file.
     */
    private String position;

    /**
     * Optional cursor marker instructions for editors that support inline comments.
     */
    private String cursorMarker;

    /**
     * 实际使用的文档格式，记录到数据库与客户端回显。
     */
    private String docFormat;

    /**
     * 注释格式（例如 JSDOC、PYTHON_DOCSTRING）。
     */
    private String commentFormat;

    /**
     * 大模型供应商，用于审计与调优。
     */
    private String modelProvider;

    /**
     * 单次推理耗时（毫秒），便于性能追踪。
     */
    private Long inferenceLatencyMs;

    /**
     * 已插入注释的完整代码（可选，便于 IDE 直接替换）。
     */
    private String annotatedCode;

    /**
     * 原始块级注释文本（未包裹代码）。
     */
    private String rawComment;

    /**
     * 行号到注释的映射，供前端高亮或旁注。
     */
    private java.util.Map<Integer, String> lineComments;
}
