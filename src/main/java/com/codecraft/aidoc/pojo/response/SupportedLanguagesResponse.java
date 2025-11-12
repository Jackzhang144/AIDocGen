package com.codecraft.aidoc.pojo.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response holder containing supported language identifiers.
 */
@Data
@Builder
public class SupportedLanguagesResponse {
    private List<String> languages;
}
