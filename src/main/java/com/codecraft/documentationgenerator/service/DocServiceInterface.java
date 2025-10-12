package com.codecraft.documentationgenerator.service;

import com.codecraft.documentationgenerator.entity.Doc;

import java.util.List;

/**
 * 文档服务接口
 * <p>
 * 提供文档相关的业务逻辑处理
 *
 * @author CodeCraft
 * @version 1.0
 */
public interface DocServiceInterface {

    /**
     * 根据ID查找文档
     *
     * @param id 文档ID
     * @return Doc 文档对象
     */
    Doc findById(Long id);

    /**
     * 根据用户ID查找文档列表
     *
     * @param userId 用户ID
     * @return List<Doc> 文档列表
     */
    List<Doc> findByUserId(Long userId);

    /**
     * 根据反馈ID查找文档
     *
     * @param feedbackId 反馈ID
     * @return Doc 文档对象
     */
    Doc findByFeedbackId(String feedbackId);

    /**
     * 创建新文档
     *
     * @param doc 文档对象
     */
    void createDoc(Doc doc);

    /**
     * 更新文档反馈
     *
     * @param doc 文档对象
     */
    void updateFeedback(Doc doc);

    /**
     * 根据ID删除文档
     *
     * @param id 文档ID
     */
    void deleteById(Long id);

    /**
     * 查找所有文档
     *
     * @return List<Doc> 所有文档列表
     */
    List<Doc> findAll();
}