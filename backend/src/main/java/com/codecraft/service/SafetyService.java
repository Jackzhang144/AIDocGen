package com.codecraft.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class SafetyService {

    private static final Map<String, Pattern> PATTERNS = Map.of(
            "潜在命令注入：Runtime.exec/ProcessBuilder", Pattern.compile("Runtime\\.getRuntime\\(\\)\\.exec|ProcessBuilder"),
            "Java 反射或类加载风险", Pattern.compile("Class\\.forName|setAccessible\\("),
            "JS 代码执行：eval/new Function", Pattern.compile("\\beval\\s*\\(|new Function"),
            "Node.js 子进程执行", Pattern.compile("child_process\\.(exec|spawn)"),
            "Node.js 删除目录/文件", Pattern.compile("fs\\.(rm|rmdir|unlink)"),
            "Shell 执行/管道", Pattern.compile("bash -c|/bin/sh"),
            "Python 系统调用", Pattern.compile("os\\.system|subprocess\\.Popen"),
            "危险 SQL 语句", Pattern.compile("(?i)(drop table|truncate table|delete from)")
    );

    public List<String> checkDangerousPatterns(String content) {
        List<String> warnings = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return warnings;
        }
        PATTERNS.forEach((message, pattern) -> {
            if (pattern.matcher(content).find()) {
                warnings.add(message);
            }
        });
        return warnings;
    }
}
