package com.codecraft.aidoc.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures OpenAPI/Knife4j metadata so consumers can browse endpoints interactively.
 */
@Configuration
public class Knife4jConfig {

    /**
     * Builds the primary OpenAPI bean with descriptive project metadata.
     *
     * @return OpenAPI descriptor
     */
    @Bean
    public OpenAPI aidocOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Project Documentation Generator API")
                        .description("Rewritten Java backend for the Project Documentation Generator platform.")
                        .version("1.0.0")
                        .contact(new Contact().name("AIDocGen Engineering").email("engineering@aidoc.internal"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("原 Node.js API 兼容说明")
                        .url("https://github.com/mintlify/ProjectDocumentationGenerator"));
    }

    /**
     * Groups public endpoints for easy navigation inside Knife4j.
     *
     * @return grouped api definition
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }
}
