package com.codecraft.aidoc.pojo.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Query parameters for paginating documentation history.
 */
@Data
public class DocHistoryQueryRequest {

    @Min(value = 1, message = "页码至少为 1")
    private Long page = 1L;

    @Min(value = 1, message = "每页条数至少为 1")
    @Max(value = 100, message = "每页最多 100 条")
    private Long size = 10L;

    /**
     * Keyword search across `prompt` and `output` fields.
     */
    private String keyword;

    /**
     * Optional language filter.
     */
    private String language;

    /**
     * Optional call source filter.
     */
    private String source;

    /**
     * Optional user filter (admin only).
     */
    private String userId;

    public String normalizedKeyword() {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }
}
