package com.codecraft.aidoc.util;

import com.codecraft.aidoc.enums.LanguageId;
import com.codecraft.aidoc.pojo.dto.CodeSynopsis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of lightweight heuristics used to derive structural hints from source code snippets.
 */
public final class CodeAnalysisUtil {

    private static final Pattern PYTHON_DEF = Pattern.compile("def\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern JS_FUNCTION = Pattern.compile("function\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern JS_ARROW = Pattern.compile("const\\s+(\\w+)\\s*=\\s*\\(([^)]*)\\)\\s*=>");
    private static final Pattern JAVA_METHOD = Pattern.compile("(?:public|protected|private|static|final|synchronized|abstract|native|strictfp|\\s)+([\\w$]+)\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern CLASS_DECLARATION = Pattern.compile("class\\s+(\\w+)");

    private CodeAnalysisUtil() {
    }

    /**
     * Derives a {@link CodeSynopsis} from the supplied snippet using best effort regular expressions.
     *
     * @param code snippet provided by the client
     * @param languageId language identifier
     * @return synopsis with fallback defaults when parsing fails
     */
    public static CodeSynopsis analyse(String code, LanguageId languageId) {
        if (code == null || code.isBlank()) {
            return CodeSynopsis.builder()
                    .kind("unknown")
                    .name("snippet")
                    .parameters(List.of())
                    .returnsValue(false)
                    .summary("代码片段信息不足，提供通用说明。")
                    .build();
        }
        String trimmed = code.strip();
        String[] lines = trimmed.split("\\r?\\n");
        String summary = Arrays.stream(lines)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .findFirst()
                .orElse("代码片段");

        if (languageId == null) {
            return fallbackSynopsis(summary);
        }

        return switch (languageId) {
            case PYTHON -> analysePython(trimmed, summary);
            case JAVASCRIPT, TYPESCRIPT, TYPESCRIPT_REACT, JAVASCRIPT_REACT -> analyseJavascript(trimmed, summary);
            case JAVA, KOTLIN -> analyseJava(trimmed, summary);
            default -> fallbackSynopsis(summary);
        };
    }

    private static CodeSynopsis analysePython(String code, String summary) {
        Matcher matcher = PYTHON_DEF.matcher(code);
        if (matcher.find()) {
            String name = matcher.group(1);
            List<String> params = extractParams(matcher.group(2));
            boolean returns = code.contains("return ");
            return CodeSynopsis.builder()
                    .kind("function")
                    .name(name)
                    .parameters(params)
                    .returnsValue(returns)
                    .summary(summary)
                    .build();
        }
        Matcher cls = CLASS_DECLARATION.matcher(code);
        if (cls.find()) {
            return CodeSynopsis.builder()
                    .kind("class")
                    .name(cls.group(1))
                    .parameters(List.of())
                    .returnsValue(false)
                    .summary(summary)
                    .build();
        }
        return fallbackSynopsis(summary);
    }

    private static CodeSynopsis analyseJavascript(String code, String summary) {
        Matcher matcher = JS_FUNCTION.matcher(code);
        if (matcher.find()) {
            return CodeSynopsis.builder()
                    .kind("function")
                    .name(matcher.group(1))
                    .parameters(extractParams(matcher.group(2)))
                    .returnsValue(code.contains("return "))
                    .summary(summary)
                    .build();
        }
        Matcher arrow = JS_ARROW.matcher(code);
        if (arrow.find()) {
            return CodeSynopsis.builder()
                    .kind("function")
                    .name(arrow.group(1))
                    .parameters(extractParams(arrow.group(2)))
                    .returnsValue(code.contains("return "))
                    .summary(summary)
                    .build();
        }
        Matcher cls = CLASS_DECLARATION.matcher(code);
        if (cls.find()) {
            return CodeSynopsis.builder()
                    .kind("class")
                    .name(cls.group(1))
                    .parameters(List.of())
                    .returnsValue(false)
                    .summary(summary)
                    .build();
        }
        return fallbackSynopsis(summary);
    }

    private static CodeSynopsis analyseJava(String code, String summary) {
        Matcher matcher = JAVA_METHOD.matcher(code);
        if (matcher.find()) {
            String returnType = matcher.group(1);
            String name = matcher.group(2);
            List<String> params = extractParams(matcher.group(3));
            boolean returns = !"void".equalsIgnoreCase(returnType);
            return CodeSynopsis.builder()
                    .kind("method")
                    .name(name)
                    .parameters(params)
                    .returnsValue(returns)
                    .summary(summary)
                    .build();
        }
        Matcher cls = CLASS_DECLARATION.matcher(code);
        if (cls.find()) {
            return CodeSynopsis.builder()
                    .kind("class")
                    .name(cls.group(1))
                    .parameters(List.of())
                    .returnsValue(false)
                    .summary(summary)
                    .build();
        }
        return fallbackSynopsis(summary);
    }

    private static List<String> extractParams(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String[] split = raw.split(",");
        List<String> params = new ArrayList<>(split.length);
        for (String part : split) {
            String cleaned = part.trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            String[] tokens = cleaned.split("\\s+");
            params.add(tokens[tokens.length - 1].replaceAll("[=).]+", "").trim());
        }
        return params;
    }

    private static CodeSynopsis fallbackSynopsis(String summary) {
        return CodeSynopsis.builder()
                .kind("snippet")
                .name("代码片段")
                .parameters(List.of())
                .returnsValue(true)
                .summary(summary.toLowerCase(Locale.ROOT))
                .build();
    }
}
