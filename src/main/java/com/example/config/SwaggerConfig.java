package com.example.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2  // 启用 Swagger
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.controller"))  // 设置扫描的包路径
                .paths(PathSelectors.any())  // 扫描所有路径
                .build()
                /*设置info    */
                .apiInfo(new ApiInfoBuilder()
                        .title("chat-service系统API")
                        .description("chat-service系统API 文档")
                        .version("1.0.0")
                        .build());
    }
}
