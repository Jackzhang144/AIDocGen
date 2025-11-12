package com.codecraft.aidoc.service;

import com.codecraft.aidoc.pojo.dto.DocGenerationResult;
import com.codecraft.aidoc.pojo.request.DocumentGenerationRequest;

/**
 * Core service responsible for analysing source code and generating documentation snippets.
 */
public interface DocumentationService {

    /**
     * Generates documentation for the supplied code sample.
     *
     * @param request request payload carrying the source code and metadata
     * @return generation result containing the documentation string and auxiliary metadata
     */
    DocGenerationResult generateDocumentation(DocumentGenerationRequest request);
}
