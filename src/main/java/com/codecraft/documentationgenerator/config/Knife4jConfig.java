package com.codecraft.documentationgenerator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置类
 * <p>
 * 配置OpenAPI规范的元数据信息，用于生成API文档
 *
 * @author CodeCraft
 * @version 1.0
 */
@Configuration
public class Knife4jConfig {

    /**
     * 自定义OpenAPI配置
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DocumentationGenerator API") // API文档标题
                        .version("1.0") // API版本
                        .description("AI驱动的代码文档生成器API文档") // API描述
                        .contact(new Contact() // 联系信息
                                .name("CodeCraft Team")
                                .url("https://github.com/codecraft")
                                .email("support@codecraft.com"))
                        .license(new License() // 许可证信息
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}