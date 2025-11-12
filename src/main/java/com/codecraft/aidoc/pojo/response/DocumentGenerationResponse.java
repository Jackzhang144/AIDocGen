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
}
