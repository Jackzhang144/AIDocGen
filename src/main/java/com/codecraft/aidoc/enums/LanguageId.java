package com.codecraft.aidoc.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Known language identifiers that the documentation generator recognises.
 */
public enum LanguageId {
    PYTHON("python"),
    JAVASCRIPT("javascript"),
    TYPESCRIPT("typescript"),
    TYPESCRIPT_REACT("typescriptreact"),
    JAVASCRIPT_REACT("javascriptreact"),
    PHP("php"),
    C("c"),
    CPP("cpp"),
    JAVA("java"),
    KOTLIN("kotlin"),
    CSHARP("csharp"),
    RUBY("ruby"),
    GO("go"),
    RUST("rust");

    private final String id;

    LanguageId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Performs a case insensitive lookup of the {@link LanguageId}.
     *
     * @param raw language identifier provided by the client
     * @return the resolved language identifier or {@code null} when unsupported
     */
    public static LanguageId from(String raw) {
        if (raw == null) {
            return null;
        }
        final String normalized = raw.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(value -> value.id.equals(normalized))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return list of public language identifiers exposed through the API.
     */
    public static List<String> publicLanguageIds() {
        return Arrays.stream(values())
                .map(LanguageId::getId)
                .toList();
    }
}
