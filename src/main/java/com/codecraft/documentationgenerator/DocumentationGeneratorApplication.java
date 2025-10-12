package com.codecraft.documentationgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * DocumentationGenerator应用程序主类
 * <p>
 * 这是一个基于Spring Boot 3的AI驱动代码文档生成器后端服务
 * 主要功能包括：
 * - 用户管理（注册、登录、订阅）
 * - 文档生成（使用AI自动生成代码文档）
 * - API密钥管理
 * - 团队协作功能
 * - 多语言代码支持
 *
 * @author CodeCraft
 * @version 1.0
 */
@SpringBootApplication
public class DocumentationGeneratorApplication {

    /**
     * 应用程序入口点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DocumentationGeneratorApplication.class, args);
    }

    /**
     * 配置跨域资源共享(CORS)
     * 允许前端应用访问后端API
     *
     * @return WebMvcConfigurer CORS配置
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 为/api路径下的所有端点配置CORS
                registry.addMapping("/api/**")
                        .allowedOrigins("*") // 允许所有来源
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                        .allowedHeaders("*"); // 允许所有请求头
            }
        };
    }
}