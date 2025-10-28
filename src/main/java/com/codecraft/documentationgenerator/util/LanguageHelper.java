package com.codecraft.documentationgenerator.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 语言辅助工具
 */
@UtilityClass
public class LanguageHelper {

    private static final Map<String, String> EXTENSION_TO_LANGUAGE = new HashMap<>();

    static {
        EXTENSION_TO_LANGUAGE.put("py", "python");
        EXTENSION_TO_LANGUAGE.put("ts", "typescript");
        EXTENSION_TO_LANGUAGE.put("tsx", "typescriptreact");
        EXTENSION_TO_LANGUAGE.put("js", "javascript");
        EXTENSION_TO_LANGUAGE.put("jsx", "javascriptreact");
        EXTENSION_TO_LANGUAGE.put("php", "php");
        EXTENSION_TO_LANGUAGE.put("java", "java");
        EXTENSION_TO_LANGUAGE.put("kt", "kotlin");
        EXTENSION_TO_LANGUAGE.put("c", "c");
        EXTENSION_TO_LANGUAGE.put("h", "c");
        EXTENSION_TO_LANGUAGE.put("cpp", "cpp");
        EXTENSION_TO_LANGUAGE.put("hpp", "cpp");
        EXTENSION_TO_LANGUAGE.put("cc", "cpp");
        EXTENSION_TO_LANGUAGE.put("cs", "csharp");
        EXTENSION_TO_LANGUAGE.put("rb", "ruby");
        EXTENSION_TO_LANGUAGE.put("rs", "rust");
        EXTENSION_TO_LANGUAGE.put("go", "go");
    }

    public static String resolveLanguageId(String fileName, String fallback) {
        if (fileName == null || fileName.isEmpty()) {
            return fallback;
        }

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return fallback;
        }

        String extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return EXTENSION_TO_LANGUAGE.getOrDefault(extension, fallback);
    }
}
