package com.codecraft.aidoc.pojo.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response payload returned for documentation generation requests.
 */
@Data
@Builder
public class DocumentGenerationResponse {

    private String documentation;

    private String annotatedCode;

    private String rawComment;

    private java.util.Map<Integer, String> lineComments;

    private String docFormat;

    private String commentFormat;

    private String modelProvider;

    private Long inferenceLatencyMs;
}
