package com.codecraft.aidoc.pojo.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * View model for documentation history rows returned to the client.
 */
@Data
@Builder
public class DocHistoryItem {

    private Long id;

    private LocalDateTime timestamp;

    private String language;

    private String source;

    private Long timeToGenerate;

    private Boolean selection;

    private Integer feedback;

    private String modelProvider;

    private String outputPreview;

    private String promptPreview;
}
