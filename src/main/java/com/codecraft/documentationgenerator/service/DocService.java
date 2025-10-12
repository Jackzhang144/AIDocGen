package com.codecraft.documentationgenerator.service;

import com.codecraft.documentationgenerator.entity.Doc;

import java.util.List;

public interface DocService {

    Doc findById(Long id);

    List<Doc> findByUserId(Long userId);

    Doc findByFeedbackId(String feedbackId);

    void createDoc(Doc doc);

    void updateFeedback(Doc doc);

    void deleteById(Long id);

    List<Doc> findAll();
}