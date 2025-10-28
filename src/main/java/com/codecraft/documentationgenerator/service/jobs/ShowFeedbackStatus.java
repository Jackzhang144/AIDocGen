package com.codecraft.documentationgenerator.service.jobs;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 控制反馈弹窗的状态
 */
@Data
@AllArgsConstructor
public class ShowFeedbackStatus {
    private boolean shouldShowFeedback;
    private boolean shouldShowShare;
}
