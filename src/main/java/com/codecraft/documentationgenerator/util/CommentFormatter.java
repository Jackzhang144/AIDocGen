package com.codecraft.documentationgenerator.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 注释格式化工具类
 */
@UtilityClass
public class CommentFormatter {

    public static String wrap(String text, int width) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (width <= 0) {
            return text;
        }

        StringBuilder builder = new StringBuilder();
        int current = 0;
        for (String word : text.split("\\s+")) {
            if (current == 0) {
                builder.append(word);
                current = word.length();
            } else if (current + 1 + word.length() > width) {
                builder.append('\n').append(word);
                current = word.length();
            } else {
                builder.append(' ').append(word);
                current += 1 + word.length();
            }
        }
        return builder.toString();
    }

    public static String addComments(String content, String languageId, String kind) {
        String commentFormat = inferCommentFormat(languageId, kind, null);
        switch (commentFormat) {
            case "JSDoc":
                return wrapBlockComment(content, "/**", "*/", " * ");
            case "PythonDocstring":
            case "NumPy":
                return "\"\"\"" + content + "\"\"\"";
            case "XML":
                return "/// <summary>" + content + "</summary>";
            case "RDoc":
                return Arrays.stream(content.split("\\n"))
                        .map(line -> "# " + line)
                        .collect(Collectors.joining("\n"));
            case "Line":
            default:
                return Arrays.stream(content.split("\\n"))
                        .map(line -> "// " + line)
                        .collect(Collectors.joining("\n"));
        }
    }

    public static String inferCommentFormat(String languageId, String kind, String docFormat) {
        String normalizedLanguage = languageId == null ? "" : languageId.toLowerCase(Locale.ROOT);
        String normalizedKind = kind == null ? "" : kind.toLowerCase(Locale.ROOT);
        String normalizedDocFormat = docFormat == null ? "" : docFormat.toLowerCase(Locale.ROOT);

        if ("java".equals(normalizedLanguage) && "class".equals(normalizedKind)) {
            return "JSDoc";
        }

        boolean isJsLike = Arrays.asList("typescript", "javascript", "typescriptreact", "javascriptreact", "php", "java", "kotlin", "c", "cpp")
                .contains(normalizedLanguage);
        if (isJsLike && ("function".equals(normalizedKind) || "typedef".equals(normalizedKind))) {
            return "JSDoc";
        }

        if ("python".equals(normalizedLanguage) && "function".equals(normalizedKind)) {
            if ("numpy".equals(normalizedDocFormat)) {
                return "NumPy";
            }
            return "PythonDocstring";
        }

        if (("csharp".equals(normalizedLanguage) && "function".equals(normalizedKind)) || "rust".equals(normalizedLanguage)) {
            return "XML";
        }

        if ("ruby".equals(normalizedLanguage) && "function".equals(normalizedKind)) {
            return "RDoc";
        }

        return "Line";
    }

    private static String wrapBlockComment(String content, String prefix, String suffix, String linePrefix) {
        String body = Arrays.stream(content.split("\\n"))
                .map(line -> linePrefix + line)
                .collect(Collectors.joining("\n"));
        return prefix + '\n' + body + '\n' + suffix;
    }
}
