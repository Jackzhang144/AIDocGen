package com.codecraft.aidoc.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Stores historical documentation generations to support analytics and feedback workflows.
 */
@Data
@TableName("docs")
public class DocEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime timestamp;

    @TableField("user_id")
    private String userId;

    private String email;

    private String output;

    private String prompt;

    private String language;

    @TableField("time_to_generate")
    private Long timeToGenerate;

    @TableField("time_to_call")
    private Long timeToCall;

    private String source;

    @TableField("feedback_id")
    private String feedbackId;

    private Integer feedback;

    @TableField("is_preview")
    private Boolean preview;

    @TableField("has_accepted_preview")
    private Boolean hasAcceptedPreview;

    @TableField("is_explained")
    private Boolean explained;

    @TableField("doc_format")
    private String docFormat;

    @TableField("comment_format")
    private String commentFormat;

    private String kind;

    @TableField("is_selection")
    private Boolean selection;

    @TableField("prompt_id")
    private String promptId;

    @TableField("actual_language")
    private String actualLanguage;

    @TableField("model_provider")
    private String modelProvider;

    @TableField("latency_ms")
    private Long latencyMs;
}
