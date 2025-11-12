package com.codecraft.aidoc.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Minimal structural representation of a code snippet used to craft meaningful documentation.
 */
@Data
@Builder
public class CodeSynopsis {

    /**
     * Human readable type (e.g. function, class).
     */
    private String kind;

    /**
     * Identifier extracted from the snippet.
     */
    private String name;

    /**
     * Collection of parameter names defined by the snippet.
     */
    private List<String> parameters;

    /**
     * Indicates whether the snippet returns a value.
     */
    private boolean returnsValue;

    /**
     * Short heuristic description derived from the code body.
     */
    private String summary;
}
