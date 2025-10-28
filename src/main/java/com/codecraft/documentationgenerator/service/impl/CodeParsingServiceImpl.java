package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.model.Synopsis;
import com.codecraft.documentationgenerator.service.CodeParsingServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 代码解析服务实现类
 * <p>
 * 提供代码解析相关的业务逻辑处理
 * 包括代码概要信息提取和代码片段提取功能
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Service
public class CodeParsingServiceImpl implements CodeParsingServiceInterface {

    /**
     * 解析代码并生成概要信息
     *
     * @param code       代码片段
     * @param languageId 编程语言标识
     * @param file       完整文件内容（可选）
     * @return 代码概要信息
     */
    public Synopsis getSynopsis(String code, String languageId, String file) {
        log.info("Getting synopsis for code in language: {}", languageId);

        Synopsis synopsis = new Synopsis();
        String trimmed = code == null ? "" : code.trim();
        String normalizedLanguage = languageId == null ? "" : languageId.toLowerCase();

        if (isClassDefinition(trimmed)) {
            synopsis.setKind("class");
            synopsis.setProperties(null);
        } else if (isInterfaceDefinition(trimmed)) {
            synopsis.setKind("typedef");
        } else if (isFunctionDefinition(trimmed, normalizedLanguage)) {
            synopsis.setKind("function");
            synopsis.setParams(ParamParser.parseParameters(trimmed));
            synopsis.setReturns(null);
        } else {
            synopsis.setKind("unspecified");
        }

        log.debug("Synopsis generated with kind: {}", synopsis.getKind());
        return synopsis;
    }

    /**
     * 根据上下文获取代码
     *
     * @param context    上下文
     * @param languageId 语言标识
     * @param location   位置
     * @param line       行内容
     * @return 代码片段
     */
    public String getCode(String context, String languageId, Integer location, String line) {
        log.info("Getting code from context in language: {}", languageId);

        if (line != null && !line.isEmpty()) {
            log.debug("Returning provided line for code extraction");
            return line;
        }

        if (context == null || context.isEmpty()) {
            return "";
        }

        if (location != null) {
            String[] lines = context.split("\\n");
            int index = Math.min(Math.max(location, 0), lines.length - 1);
            return lines[index];
        }

        return context;
    }

    private boolean isClassDefinition(String code) {
        return code.startsWith("class ") || code.contains(" class ");
    }

    private boolean isInterfaceDefinition(String code) {
        return code.startsWith("interface ") || code.startsWith("type ") || code.contains(" interface ");
    }

    private boolean isFunctionDefinition(String code, String languageId) {
        return code.matches("(?s).*(def |function |fun |[a-zA-Z0-9_]+\\s*\\().*")
                || ("javascript".equals(languageId) && code.contains("=>"));
    }

    private static class ParamParser {
        static java.util.List<Synopsis.Param> parseParameters(String code) {
            java.util.List<Synopsis.Param> params = new java.util.ArrayList<>();
            int openIndex = code.indexOf('(');
            int closeIndex = code.indexOf(')');
            if (openIndex < 0 || closeIndex < 0 || closeIndex <= openIndex) {
                return params;
            }

            String inside = code.substring(openIndex + 1, closeIndex);
            if (inside.trim().isEmpty()) {
                return params;
            }

            String[] rawParams = inside.split(",");
            for (String raw : rawParams) {
                String cleaned = raw.trim();
                if (cleaned.isEmpty()) {
                    continue;
                }
                Synopsis.Param param = new Synopsis.Param();
                String typePart = null;
                if (cleaned.contains(":")) {
                    String[] colonSplit = cleaned.split(":", 2);
                    cleaned = colonSplit[0].trim();
                    typePart = colonSplit[1].trim();
                }

                String[] tokens = cleaned.split("\\s+");
                String nameToken = tokens[tokens.length - 1].replaceAll("=.*$", "");
                param.setName(nameToken);
                if (typePart != null && !typePart.isEmpty()) {
                    param.setType(typePart);
                } else if (tokens.length > 1) {
                    param.setType(tokens[0]);
                }
                params.add(param);
            }
            return params;
        }
    }
}
