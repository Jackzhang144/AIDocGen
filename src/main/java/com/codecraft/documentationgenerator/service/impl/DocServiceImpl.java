package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.constant.MessageConstants;
import com.codecraft.documentationgenerator.entity.Doc;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.mapper.DocMapper;
import com.codecraft.documentationgenerator.service.DocServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档服务实现类
 * <p>
 * 实现文档相关的业务逻辑
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Service
public class DocServiceImpl implements DocServiceInterface {

    @Autowired
    private DocMapper docMapper;

    /**
     * 根据ID查找文档
     *
     * @param id 文档ID
     * @return Doc 文档对象
     */
    public Doc findById(Long id) {
        log.info("Finding document by ID: {}", id);
        Doc doc = docMapper.findById(id);
        if (doc == null) {
            throw new BusinessException(MessageConstants.DOCUMENT_NOT_FOUND);
        }
        return doc;
    }

    /**
     * 根据用户ID查找文档列表
     *
     * @param userId 用户ID
     * @return List<Doc> 文档列表
     */
    public List<Doc> findByUserId(String userId) {
        log.info("Finding documents by user ID: {}", userId);
        return docMapper.findByUserId(userId);
    }

    /**
     * 根据反馈ID查找文档
     *
     * @param feedbackId 反馈ID
     * @return Doc 文档对象
     */
    public Doc findByFeedbackId(String feedbackId) {
        log.info("Finding document by feedback ID: {}", feedbackId);
        Doc doc = docMapper.findByFeedbackId(feedbackId);
        if (doc == null) {
            throw new BusinessException(MessageConstants.DOCUMENT_NOT_FOUND);
        }
        return doc;
    }

    /**
     * 创建新文档
     *
     * @param doc 文档对象
     */
    public void createDoc(Doc doc) {
        log.info("Creating new document for user ID: {}", doc.getUserId());
        docMapper.insert(doc);
    }

    /**
     * 更新文档反馈
     *
     * @param doc 文档对象
     */
    public void updateFeedback(Doc doc) {
        log.info("Updating feedback for document ID: {}", doc.getId());
        docMapper.updateFeedback(doc);
    }

    /**
     * 统计用户在指定时间段内生成的文档数量
     *
     * @param userId 用户ID
     * @param since  起始时间
     * @return 生成的文档数量
     */
    public int countDocsByUserSince(String userId, LocalDateTime since) {
        log.info("Counting documents for user {} since {}", userId, since);
        return docMapper.countDocsByUserSince(userId, since);
    }

    /**
     * 判断用户是否有正向反馈
     *
     * @param userId 用户ID
     * @return 是否存在正向反馈
     */
    public boolean hasPositiveFeedback(String userId) {
        log.info("Checking positive feedback for user {}", userId);
        return docMapper.hasPositiveFeedback(userId);
    }

    /**
     * 获取用户最近的文档
     *
     * @param userId 用户ID
     * @param limit  返回数量
     * @return 文档列表
     */
    public List<Doc> findRecentDocs(String userId, int limit) {
        log.info("Fetching {} recent documents for user {}", limit, userId);
        return docMapper.findRecentDocs(userId, limit);
    }

    /**
     * 根据ID删除文档
     *
     * @param id 文档ID
     */
    public void deleteById(Long id) {
        log.info("Deleting document with ID: {}", id);
        Doc doc = docMapper.findById(id);
        if (doc == null) {
            throw new BusinessException(MessageConstants.DOCUMENT_NOT_FOUND);
        }
        docMapper.deleteById(id);
    }

    /**
     * 查找所有文档
     *
     * @return List<Doc> 所有文档列表
     */
    public List<Doc> findAll() {
        log.info("Finding all documents");
        return docMapper.findAll();
    }
}
