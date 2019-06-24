package com.marand.meds.app.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Mitja Lapajne
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig
{
  @Bean
  public Docket swagger()
  {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.marand.meds.rest.meds"))
        .paths(PathSelectors.ant("/api/**"))
        .build()
        .pathMapping("/")
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo()
  {
    return new ApiInfo(
        "OPENeP API",
        "API documentation for OPENeP application",
        null,
        null,
        new Contact("Marand Meds", null, null),
        null,
        null,
        Collections.emptyList());
  }

}
