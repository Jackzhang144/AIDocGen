package com.codecraft.documentationgenerator.service.jobs;

import lombok.Data;

/**
 * 文档生成任务的结果
 */
@Data
public class DocJobResult {
    private String docstring;
    private String position;
    private String feedbackId;
    private String preview;
    private CursorMarker cursorMarker;
    private boolean shouldShowFeedback;
    private boolean shouldShowShare;

    @Data
    public static class CursorMarker {
        private Integer line;
        private String message;
    }
}
