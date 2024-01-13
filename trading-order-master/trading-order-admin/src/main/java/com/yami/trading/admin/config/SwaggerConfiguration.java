
package com.yami.trading.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger文档，只有在测试环境才会使用
 *
 * @author LGH
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket createRestApi() {

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("外汇管理平台")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.yami.trading"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public ApiInfo apiInfo() {

        return new ApiInfoBuilder()
                .title("外汇管理平台接口文档")
                .description("外汇管理平台接口文档Swagger版")
                .termsOfServiceUrl("")
                .contact(new Contact("", "", ""))
                .version("1.0")
                .build();
    }

}
