package com.codecraft.aidoc.service.impl;

import com.codecraft.aidoc.enums.CommentFormat;
import com.codecraft.aidoc.enums.DocFormat;
import com.codecraft.aidoc.enums.ErrorCode;
import com.codecraft.aidoc.enums.LanguageId;
import com.codecraft.aidoc.exception.BusinessException;
import com.codecraft.aidoc.gateway.ModelGateway;
import com.codecraft.aidoc.gateway.ModelGatewayRequest;
import com.codecraft.aidoc.gateway.ModelGatewayResult;
import com.codecraft.aidoc.pojo.dto.CodeSynopsis;
import com.codecraft.aidoc.pojo.dto.DocGenerationResult;
import com.codecraft.aidoc.pojo.request.DocumentGenerationRequest;
import com.codecraft.aidoc.service.DocumentationService;
import com.codecraft.aidoc.util.CodeAnalysisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 负责协调本地启发式逻辑与大模型网关，生成高质量的代码文档。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentationServiceImpl implements DocumentationService {

    private final ModelGateway modelGateway;

    @Override
    public DocGenerationResult generateDocumentation(DocumentGenerationRequest request) {
        LanguageId languageId = LanguageId.from(request.getLanguage());
        if (languageId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "暂不支持的语言标识: " + request.getLanguage());
        }

        CodeSynopsis synopsis = CodeAnalysisUtil.analyse(request.getCode(), languageId);
        DocFormat requestedFormat = DocFormat.fromExternalId(request.getFormat());
        DocFormat format = determineFormat(languageId, requestedFormat);
        CommentFormat commentFormat = resolveCommentFormat(languageId, synopsis, format);

        ModelGatewayRequest gatewayRequest = ModelGatewayRequest.builder()
                .code(request.getCode())
                .context(request.getContext())
                .languageId(languageId)
                .docFormat(format)
                .commentFormat(commentFormat)
                .commented(Boolean.TRUE.equals(request.getCommented()))
                .synopsis(synopsis)
                .width(request.getWidth())
                .quality(request.getQuality())
                .lineCommentRatio(request.getLineCommentRatio())
                .build();

        ModelGatewayResult gatewayResult = modelGateway.generateDocstring(gatewayRequest)
                .orElseGet(() -> createFallbackResult(format, synopsis));

        String docBody = StringUtils.hasText(gatewayResult.getContent())
                ? gatewayResult.getContent()
                : renderDocBody(format, synopsis);

        String headerComment = docBody;
        if (Boolean.TRUE.equals(request.getCommented())) {
            docBody = wrapWithComments(docBody, languageId, commentFormat);
        }

        String annotatedCode = buildAnnotatedCode(request.getCode(), languageId, commentFormat, headerComment, request.getLineCommentRatio());

        log.info("[AIDocGen] 文档生成完成 target={} provider={} format={}", synopsis.getName(),
                gatewayResult.getProvider(), format.getId());

        return DocGenerationResult.builder()
                .documentation(docBody)
                .annotatedCode(annotatedCode)
                .rawComment(headerComment)
                .lineComments(Map.of())
                .preview(truncate(docBody, 240))
                .position(languageId == LanguageId.PYTHON ? "BelowStartLine" : "Above")
                .feedbackId(null)
                .cursorMarker(null)
                .docFormat(format.getId())
                .commentFormat(commentFormat.name())
                .modelProvider(gatewayResult.getProvider())
                .inferenceLatencyMs(gatewayResult.getLatencyMs())
                .build();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private DocFormat determineFormat(LanguageId languageId, DocFormat requested) {
        if (requested != null && requested != DocFormat.AUTO_DETECT) {
            return requested;
        }
        return switch (languageId) {
            case PYTHON -> DocFormat.GOOGLE;
            case JAVA, KOTLIN -> DocFormat.JAVADOC;
            case JAVASCRIPT, TYPESCRIPT, TYPESCRIPT_REACT, JAVASCRIPT_REACT -> DocFormat.JSDOC;
            case PHP, C, CPP -> DocFormat.DOC_BLOCK;
            default -> DocFormat.GOOGLE;
        };
    }

    private String renderDocBody(DocFormat format, CodeSynopsis synopsis) {
        return switch (format) {
            case JSDOC, JAVADOC -> buildJsDocLike(format, synopsis);
            case REST -> buildReStructuredText(synopsis);
            case NUMPY -> buildNumPy(synopsis);
            case DOC_BLOCK, DOXYGEN -> buildDocBlock(format, synopsis);
            case GOOGLE -> buildGoogle(synopsis);
            case AUTO_DETECT -> buildGoogle(synopsis);
        };
    }

    private String buildJsDocLike(DocFormat format, CodeSynopsis synopsis) {
        StringBuilder builder = new StringBuilder();
        builder.append(capitaliseSummary(synopsis.getSummary())).append('\n');
        for (String param : synopsis.getParameters()) {
            builder.append("@param ")
                    .append(param)
                    .append(" description of ")
                    .append(param)
                    .append('\n');
        }
        if (synopsis.isReturnsValue()) {
            builder.append(format == DocFormat.JAVADOC ? "@return " : "@returns ")
                    .append("result computed by ")
                    .append(synopsis.getName())
                    .append('.');
        }
        return builder.toString().strip();
    }

    private String buildReStructuredText(CodeSynopsis synopsis) {
        StringBuilder builder = new StringBuilder();
        builder.append(".. function:: ")
                .append(synopsis.getName())
                .append('(')
                .append(String.join(", ", synopsis.getParameters()))
                .append(")\n\n")
                .append(indent(synopsis.getSummary(), 3))
                .append("\n\n");
        for (String param : synopsis.getParameters()) {
            builder.append(indent(":param " + param + ": 参数 " + param + " 的用途", 3))
                    .append('\n');
        }
        if (synopsis.isReturnsValue()) {
            builder.append(indent(":returns: 函数返回值", 3));
        }
        return builder.toString().strip();
    }

    private String buildNumPy(CodeSynopsis synopsis) {
        StringBuilder builder = new StringBuilder();
        builder.append(synopsis.getSummary()).append("\n\n");
        if (!synopsis.getParameters().isEmpty()) {
            builder.append("Parameters\n----------\n");
            for (String param : synopsis.getParameters()) {
                builder.append(param).append(" : Any\n    描述参数 ").append(param).append(" 的作用\n");
            }
        }
        if (synopsis.isReturnsValue()) {
            builder.append("\nReturns\n-------\nAny\n    函数的返回结果\n");
        }
        return builder.toString().strip();
    }

    private String buildDocBlock(DocFormat format, CodeSynopsis synopsis) {
        StringBuilder builder = new StringBuilder();
        builder.append(capitaliseSummary(synopsis.getSummary())).append('\n');
        for (String param : synopsis.getParameters()) {
            builder.append("@param ").append(param).append(" 描述").append('\n');
        }
        if (synopsis.isReturnsValue()) {
            builder.append(format == DocFormat.DOC_BLOCK ? "@return 返回值说明" : "@return 返回值说明");
        }
        return builder.toString().strip();
    }

    private String buildGoogle(CodeSynopsis synopsis) {
        StringBuilder builder = new StringBuilder();
        builder.append(capitaliseSummary(synopsis.getSummary())).append("\n\n");
        if (!synopsis.getParameters().isEmpty()) {
            builder.append("Args:\n");
            for (String param : synopsis.getParameters()) {
                builder.append("    ").append(param).append(" (Any): 描述参数 ").append(param).append(" 的用途.\n");
            }
        }
        if (synopsis.isReturnsValue()) {
            builder.append("Returns:\n    Any: 函数执行后的结果.\n");
        }
        return builder.toString().strip();
    }

    private String wrapWithComments(String docBody, LanguageId languageId, CommentFormat commentFormat) {
        List<String> lines = docBody.lines().map(String::trim).toList();
        return switch (commentFormat) {
            case PYTHON_DOCSTRING, NUMPY -> wrapPython(lines);
            case JSDOC -> wrapBlockComment(lines);
            case XML -> wrapXml(lines);
            case RDOC -> wrapRuby(lines);
            case LINE -> wrapLineComment(lines, languageId);
        };
    }

    private String wrapPython(List<String> lines) {
        StringBuilder builder = new StringBuilder("\"\"\"\n");
        for (String line : lines) {
            builder.append(line).append('\n');
        }
        builder.append("\"\"\"");
        return builder.toString();
    }

    private String wrapBlockComment(List<String> lines) {
        StringBuilder builder = new StringBuilder("/**\n");
        for (String line : lines) {
            builder.append(" * ").append(line).append('\n');
        }
        builder.append(" */");
        return builder.toString();
    }

    private String wrapXml(List<String> lines) {
        StringBuilder builder = new StringBuilder("/// <summary>\n");
        for (String line : lines) {
            builder.append("/// ").append(line).append('\n');
        }
        builder.append("/// </summary>");
        return builder.toString();
    }

    private String wrapRuby(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append("# ").append(line).append('\n');
        }
        return builder.toString().stripTrailing();
    }

    private String wrapLineComment(List<String> lines, LanguageId languageId) {
        String prefix = switch (languageId) {
            case RUST -> "///";
            case GO -> "//";
            default -> "//";
        };
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(prefix).append(' ').append(line).append('\n');
        }
        return builder.toString().stripTrailing();
    }

    private String indent(String value, int spaces) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String indent = " ".repeat(spaces);
        return indent + value.replace("\n", "\n" + indent);
    }

    private String capitaliseSummary(String summary) {
        if (!StringUtils.hasText(summary)) {
            return "Generated documentation";
        }
        String trimmed = summary.trim();
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    private CommentFormat resolveCommentFormat(LanguageId languageId, CodeSynopsis synopsis, DocFormat docFormat) {
        String kind = synopsis.getKind() == null ? "" : synopsis.getKind().toLowerCase(Locale.ROOT);
        boolean isFunctionLike = kind.contains("function") || kind.contains("method") || kind.contains("typedef");
        if (languageId == LanguageId.JAVA && kind.contains("class")) {
            return CommentFormat.JSDOC;
        }
        if ((languageId == LanguageId.TYPESCRIPT
                || languageId == LanguageId.TYPESCRIPT_REACT
                || languageId == LanguageId.JAVASCRIPT
                || languageId == LanguageId.JAVASCRIPT_REACT
                || languageId == LanguageId.PHP
                || languageId == LanguageId.JAVA
                || languageId == LanguageId.KOTLIN
                || languageId == LanguageId.C
                || languageId == LanguageId.CPP) && isFunctionLike) {
            return CommentFormat.JSDOC;
        }
        if (languageId == LanguageId.PYTHON && isFunctionLike) {
            return docFormat == DocFormat.NUMPY ? CommentFormat.NUMPY : CommentFormat.PYTHON_DOCSTRING;
        }
        if ((languageId == LanguageId.CSHARP && isFunctionLike) || languageId == LanguageId.RUST) {
            return CommentFormat.XML;
        }
        if (languageId == LanguageId.RUBY && isFunctionLike) {
            return CommentFormat.RDOC;
        }
        return CommentFormat.LINE;
    }

    private ModelGatewayResult createFallbackResult(DocFormat format, CodeSynopsis synopsis) {
        String docBody = renderDocBody(format, synopsis);
        return ModelGatewayResult.builder()
                .content(docBody)
                .provider("heuristic")
                .latencyMs(0L)
                .fallback(true)
                .build();
    }

    /**
     * 简单的注释注入器：在代码前添加头部注释，并可选地为方法定义行添加行级注释。
     */
    private String buildAnnotatedCode(String code, LanguageId languageId, CommentFormat commentFormat, String header, Double lineCommentRatio) {
        if (!StringUtils.hasText(code)) {
            return header;
        }
        // 头部注释
        StringBuilder builder = new StringBuilder();
        builder.append(wrapWithComments(header, languageId, commentFormat)).append("\n");

        String[] lines = code.split("\\r?\\n");
        double ratio = lineCommentRatio == null ? 0.3 : Math.max(0, Math.min(1, lineCommentRatio));
        boolean annotateFirst = ratio > 0.01;
        String linePrefix = switch (commentFormat) {
            case JSDOC -> "//";
            case PYTHON_DOCSTRING, NUMPY -> "#";
            case XML -> "///";
            case RDOC -> "#";
            case LINE -> "//";
        };
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            builder.append(line);
            if (annotateFirst && i == 0 && StringUtils.hasText(line)) {
                builder.append(" ").append(linePrefix).append(" ").append("TODO: explain this line");
            }
            builder.append("\n");
        }
        return builder.toString().stripTrailing();
    }
}
