package org.resource.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableWebMvc
@PropertySource(value = "file:src/main/resources/application.properties")
public class SwaggerConfig {

   @Bean
   public Docket api() {
      return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("org.resource.controller"))
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build();
   }

//   @Bean
//   public MultipartResolver multipartResolver() {
//      CommonsMultipartResolver multipartResolver
//            = new CommonsMultipartResolver();
//      multipartResolver.setMaxUploadSize(50242880);
//      return multipartResolver;
//   }

}
