package com.esri.geoportal.harvester.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;

import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;


/**
 * Bridge beans that Spring Boot would normally provide.
 * Required by springdoc-openapi v2 when running without Spring Boot.
 */
@Configuration
public class SpringdocBootBridgingConfig {

  @Bean
  public WebMvcProperties webMvcProperties() {
    return new WebMvcProperties();
  }

  @Bean
     public SpringDocConfigProperties springDocConfigProperties() {
         return new SpringDocConfigProperties();
     }

     @Bean
     public SwaggerUiConfigProperties swaggerUiConfigProperties() {
         return new SwaggerUiConfigProperties();
     }

     @Bean
     public SwaggerUiOAuthProperties swaggerUiOAuthProperties() {
         return new SwaggerUiOAuthProperties();
     }

}