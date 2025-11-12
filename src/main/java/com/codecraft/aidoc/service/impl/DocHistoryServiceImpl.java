package com.codecraft.aidoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecraft.aidoc.common.PageResponse;
import com.codecraft.aidoc.mapper.DocMapper;
import com.codecraft.aidoc.pojo.entity.DocEntity;
import com.codecraft.aidoc.pojo.request.DocHistoryQueryRequest;
import com.codecraft.aidoc.pojo.response.DocHistoryItem;
import com.codecraft.aidoc.service.DocHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation backed by MyBatis-Plus pagination.
 */
@Service
@RequiredArgsConstructor
public class DocHistoryServiceImpl implements DocHistoryService {

    private static final int PREVIEW_LIMIT = 320;

    private final DocMapper docMapper;

    @Override
    public PageResponse<DocHistoryItem> pageHistory(DocHistoryQueryRequest request) {
        long page = request.getPage() != null ? request.getPage() : 1;
        long size = request.getSize() != null ? request.getSize() : 10;
        Page<DocEntity> mpPage = new Page<>(page, size);

        LambdaQueryWrapper<DocEntity> wrapper = new LambdaQueryWrapper<>();
        String keyword = request.normalizedKeyword();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(DocEntity::getOutput, keyword)
                    .or().like(DocEntity::getPrompt, keyword)
                    .or().like(DocEntity::getFeedbackId, keyword));
        }
        if (StringUtils.hasText(request.getLanguage())) {
            wrapper.eq(DocEntity::getLanguage, request.getLanguage());
        }
        if (StringUtils.hasText(request.getSource())) {
            wrapper.eq(DocEntity::getSource, request.getSource());
        }
        if (StringUtils.hasText(request.getUserId())) {
            wrapper.eq(DocEntity::getUserId, request.getUserId());
        }
        wrapper.orderByDesc(DocEntity::getTimestamp);

        Page<DocEntity> resultPage = docMapper.selectPage(mpPage, wrapper);
        List<DocHistoryItem> records = resultPage.getRecords().stream()
                .map(this::toView)
                .collect(Collectors.toList());

        return PageResponse.of(resultPage.getTotal(), page, size, records);
    }

    private DocHistoryItem toView(DocEntity entity) {
        return DocHistoryItem.builder()
                .id(entity.getId())
                .timestamp(entity.getTimestamp())
                .language(entity.getLanguage())
                .source(entity.getSource())
                .timeToGenerate(entity.getTimeToGenerate())
                .selection(entity.getSelection())
                .feedback(entity.getFeedback())
                .modelProvider(entity.getModelProvider())
                .outputPreview(crop(entity.getOutput()))
                .promptPreview(crop(entity.getPrompt()))
                .build();
    }

    private String crop(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        if (source.length() <= PREVIEW_LIMIT) {
            return source;
        }
        return source.substring(0, PREVIEW_LIMIT) + "...";
    }
}
