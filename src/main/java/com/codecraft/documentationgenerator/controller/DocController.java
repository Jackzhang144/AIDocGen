package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.aop.RequireLogin;
import com.codecraft.documentationgenerator.entity.Doc;
import com.codecraft.documentationgenerator.service.DocServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档控制器
 * <p>
 * 处理文档相关的RESTful API请求
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/docs")
public class DocController {

    @Autowired
    private DocServiceInterface docService;

    /**
     * 根据ID获取文档
     *
     * @param id 文档ID
     * @return Doc 文档对象
     */
    @RequireLogin
    @GetMapping("/{id}")
    public Doc getDocById(@PathVariable Long id) {
        log.info("Fetching document by ID: {}", id);
        return docService.findById(id);
    }

    /**
     * 根据用户ID获取文档列表
     *
     * @param userId 用户ID
     * @return List<Doc> 文档列表
     */
    @RequireLogin
    @GetMapping("/user/{userId}")
    public List<Doc> getDocsByUserId(@PathVariable String userId) {
        log.info("Fetching documents for user ID: {}", userId);
        return docService.findByUserId(userId);
    }

    /**
     * 根据反馈ID获取文档
     *
     * @param feedbackId 反馈ID
     * @return Doc 文档对象
     */
    @RequireLogin
    @GetMapping("/feedback/{feedbackId}")
    public Doc getDocByFeedbackId(@PathVariable String feedbackId) {
        log.info("Fetching document by feedback ID: {}", feedbackId);
        return docService.findByFeedbackId(feedbackId);
    }

    /**
     * 创建新文档
     *
     * @param doc 文档对象
     */
    @RequireLogin
    @PostMapping
    public void createDoc(@RequestBody Doc doc) {
        log.info("Creating new document");
        docService.createDoc(doc);
    }

    /**
     * 更新文档反馈
     *
     * @param doc 文档对象
     */
    @RequireLogin
    @PutMapping("/feedback")
    public void updateFeedback(@RequestBody Doc doc) {
        log.info("Updating feedback for document ID: {}", doc.getId());
        docService.updateFeedback(doc);
    }

    /**
     * 删除指定ID的文档
     *
     * @param id 文档ID
     */
    @RequireLogin
    @DeleteMapping("/{id}")
    public void deleteDoc(@PathVariable Long id) {
        log.info("Deleting document with ID: {}", id);
        docService.deleteById(id);
    }

    /**
     * 获取所有文档列表
     *
     * @return List<Doc> 所有文档列表
     */
    @RequireLogin
    @GetMapping
    public List<Doc> getAllDocs() {
        log.info("Fetching all documents");
        return docService.findAll();
    }
}
