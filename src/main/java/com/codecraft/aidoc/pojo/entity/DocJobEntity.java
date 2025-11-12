package com.codecraft.aidoc.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.codecraft.aidoc.enums.JobState;
import lombok.Data;
import org.apache.ibatis.type.EnumTypeHandler;

import java.time.LocalDateTime;

/**
 * Persistent representation of an asynchronous documentation generation job.
 */
@Data
@TableName("doc_jobs")
public class DocJobEntity {

    @TableId("job_id")
    private String jobId;

    @TableField("user_id")
    private Long userId;

    @TableField(value = "state", typeHandler = EnumTypeHandler.class)
    private JobState state;

    private String reason;

    private String payload;

    private String result;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
