package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.Doc;
import com.codecraft.documentationgenerator.mapper.DocMapper;
import com.codecraft.documentationgenerator.service.DocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocServiceImpl implements DocService {

    @Autowired
    private DocMapper docMapper;

    public Doc findById(Long id) {
        return docMapper.findById(id);
    }

    public List<Doc> findByUserId(Long userId) {
        return docMapper.findByUserId(userId);
    }

    public Doc findByFeedbackId(String feedbackId) {
        return docMapper.findByFeedbackId(feedbackId);
    }

    public void createDoc(Doc doc) {
        docMapper.insert(doc);
    }

    public void updateFeedback(Doc doc) {
        docMapper.updateFeedback(doc);
    }

    public void deleteById(Long id) {
        docMapper.deleteById(id);
    }

    public List<Doc> findAll() {
        return docMapper.findAll();
    }
}