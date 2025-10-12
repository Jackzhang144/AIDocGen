package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.Doc;
import com.codecraft.documentationgenerator.service.DocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docs")
public class DocController {

    @Autowired
    private DocService docService;

    @GetMapping("/{id}")
    public Doc getDocById(@PathVariable Long id) {
        return docService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Doc> getDocsByUserId(@PathVariable Long userId) {
        return docService.findByUserId(userId);
    }

    @GetMapping("/feedback/{feedbackId}")
    public Doc getDocByFeedbackId(@PathVariable String feedbackId) {
        return docService.findByFeedbackId(feedbackId);
    }

    @PostMapping
    public void createDoc(@RequestBody Doc doc) {
        docService.createDoc(doc);
    }

    @PutMapping("/feedback")
    public void updateFeedback(@RequestBody Doc doc) {
        docService.updateFeedback(doc);
    }

    @DeleteMapping("/{id}")
    public void deleteDoc(@PathVariable Long id) {
        docService.deleteById(id);
    }

    @GetMapping
    public List<Doc> getAllDocs() {
        return docService.findAll();
    }
}