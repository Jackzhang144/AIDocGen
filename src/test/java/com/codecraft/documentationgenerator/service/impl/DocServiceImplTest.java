package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.Doc;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.mapper.DocMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DocServiceImplTest {

    @Mock
    private DocMapper docMapper;

    @InjectMocks
    private DocServiceImpl docService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_DocExists_ReturnsDoc() {
        // Given
        Doc expectedDoc = new Doc();
        expectedDoc.setId(1L);
        expectedDoc.setPrompt("Test Document Prompt");
        when(docMapper.findById(1L)).thenReturn(expectedDoc);

        // When
        Doc actualDoc = docService.findById(1L);

        // Then
        assertNotNull(actualDoc);
        assertEquals(expectedDoc.getId(), actualDoc.getId());
        assertEquals(expectedDoc.getPrompt(), actualDoc.getPrompt());
        verify(docMapper).findById(1L);
    }

    @Test
    void findById_DocNotExists_ThrowsBusinessException() {
        // Given
        when(docMapper.findById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> docService.findById(1L));
        assertEquals("文档不存在", exception.getMessage());
        verify(docMapper).findById(1L);
    }

    @Test
    void findByFeedbackId_DocExists_ReturnsDoc() {
        // Given
        Doc expectedDoc = new Doc();
        expectedDoc.setId(1L);
        expectedDoc.setFeedbackId("fb123");
        expectedDoc.setPrompt("Test Document Prompt");
        when(docMapper.findByFeedbackId("fb123")).thenReturn(expectedDoc);

        // When
        Doc actualDoc = docService.findByFeedbackId("fb123");

        // Then
        assertNotNull(actualDoc);
        assertEquals(expectedDoc.getFeedbackId(), actualDoc.getFeedbackId());
        assertEquals(expectedDoc.getPrompt(), actualDoc.getPrompt());
        verify(docMapper).findByFeedbackId("fb123");
    }

    @Test
    void findByFeedbackId_DocNotExists_ThrowsBusinessException() {
        // Given
        when(docMapper.findByFeedbackId(anyString())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> docService.findByFeedbackId("nonexistent"));
        assertEquals("文档不存在", exception.getMessage());
        verify(docMapper).findByFeedbackId("nonexistent");
    }

    @Test
    void createDoc_ValidDoc_CreatesDoc() {
        // Given
        Doc doc = new Doc();
        doc.setPrompt("New Document Prompt");

        // When
        docService.createDoc(doc);

        // Then
        verify(docMapper).insert(any(Doc.class));
    }

    @Test
    void deleteById_DocExists_DeletesDoc() {
        // Given
        Doc doc = new Doc();
        doc.setId(1L);
        when(docMapper.findById(1L)).thenReturn(doc);

        // When
        docService.deleteById(1L);

        // Then
        verify(docMapper).deleteById(1L);
    }

    @Test
    void deleteById_DocNotExists_ThrowsBusinessException() {
        // Given
        when(docMapper.findById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> docService.deleteById(1L));
        assertEquals("文档不存在", exception.getMessage());
        verify(docMapper, never()).deleteById(anyLong());
    }

    @Test
    void findAll_ReturnsAllDocs() {
        // Given
        Doc doc1 = new Doc();
        doc1.setId(1L);
        doc1.setPrompt("Document 1 Prompt");
        
        Doc doc2 = new Doc();
        doc2.setId(2L);
        doc2.setPrompt("Document 2 Prompt");
        
        List<Doc> expectedDocs = Arrays.asList(doc1, doc2);
        when(docMapper.findAll()).thenReturn(expectedDocs);

        // When
        List<Doc> actualDocs = docService.findAll();

        // Then
        assertNotNull(actualDocs);
        assertEquals(2, actualDocs.size());
        assertEquals(expectedDocs, actualDocs);
        verify(docMapper).findAll();
    }

    @Test
    void findByUserId_ReturnsUserDocs() {
        // Given
        Doc doc1 = new Doc();
        doc1.setId(1L);
        doc1.setUserId(1L);
        doc1.setPrompt("User Document 1 Prompt");
        
        Doc doc2 = new Doc();
        doc2.setId(2L);
        doc2.setUserId(1L);
        doc2.setPrompt("User Document 2 Prompt");
        
        List<Doc> expectedDocs = Arrays.asList(doc1, doc2);
        when(docMapper.findByUserId(1L)).thenReturn(expectedDocs);

        // When
        List<Doc> actualDocs = docService.findByUserId(1L);

        // Then
        assertNotNull(actualDocs);
        assertEquals(2, actualDocs.size());
        assertEquals(expectedDocs, actualDocs);
        verify(docMapper).findByUserId(1L);
    }
}