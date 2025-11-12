package com.codecraft.aidoc.enums;

import java.util.List;

/**
 * Enumerates the supported documentation formats exposed through the public API.
 * Each format carries a stable identifier and the set of languages that use it by default.
 */
public enum DocFormat {

    /**
     * JavaScript inspired docstring block that also works for TypeScript and C-style languages.
     */
    JSDOC("JSDoc", List.of("javascript", "typescript", "javascriptreact", "typescriptreact", "java", "kotlin")),

    /**
     * reStructuredText layout, commonly consumed by Sphinx documentation builds.
     */
    REST("reST", List.of("python")),

    /**
     * NumPy flavour docstring layout for scientific Python stacks.
     */
    NUMPY("NumPy", List.of("python")),

    /**
     * PHPDoc / DocBlock layout popular across PHP and C/C++ ecosystems.
     */
    DOC_BLOCK("DocBlock", List.of("php", "c", "cpp")),

    /**
     * Doxygen layout for documentation that targets C and C++ codebases.
     */
    DOXYGEN("Doxygen", List.of("c", "cpp")),

    /**
     * Standard Javadoc block that integrates with the Java tooling chain.
     */
    JAVADOC("Javadoc", List.of("java", "kotlin")),

    /**
     * Google style docstrings, frequently used in Python projects.
     */
    GOOGLE("Google", List.of("python", "go")),

    /**
     * Auto detection flag - lets the service choose an appropriate style.
     */
    AUTO_DETECT("Auto-detect", List.of());

    private final String id;
    private final List<String> defaultLanguages;

    DocFormat(String id, List<String> defaultLanguages) {
        this.id = id;
        this.defaultLanguages = defaultLanguages;
    }

    public String getId() {
        return id;
    }

    public List<String> getDefaultLanguages() {
        return defaultLanguages;
    }

    /**
     * Performs a case-insensitive lookup by identifier, falling back to auto detection when
     * the requested identifier is unknown.
     *
     * @param externalId identifier received from clients
     * @return resolved documentation format, never {@code null}
     */
    public static DocFormat fromExternalId(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            return AUTO_DETECT;
        }
        for (DocFormat value : values()) {
            if (value.id.equalsIgnoreCase(externalId) || value.name().equalsIgnoreCase(externalId)) {
                return value;
            }
        }
        return AUTO_DETECT;
    }
}
