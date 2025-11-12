package com.codecraft.aidoc.pojo.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response payload listing supported documentation formats and their defaults.
 */
@Data
@Builder
public class SupportedFormatsResponse {

    /**
     * Documentation formats exposed by the API.
     */
    private List<FormatDescriptor> formats;

    /**
     * Nested descriptor that keeps API responses succinct.
     */
    @Data
    @Builder
    public static class FormatDescriptor {
        private String id;
        private List<String> defaultLanguages;
    }
}
