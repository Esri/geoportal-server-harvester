package com.esri.geoportal.harvester.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.ServletContext;

@Configuration
public class SpringdocConfig {
    private final Logger LOG = LoggerFactory.getLogger(SpringdocConfig.class);

    @Bean
    public WebMvcProperties webMvcProperties() {
        return new WebMvcProperties();
    }

    @Bean
    public SpringDocConfigProperties springDocConfigProperties() {
        return new SpringDocConfigProperties();
    }

    @Bean
    public SwaggerUiConfigProperties swaggerUiConfigProperties(ServletContext servletContext) {
        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();
        String ctx = servletContext.getContextPath();
        String base = (ctx == null || ctx.isEmpty() ? "" : ctx);

        //Swagger UI where OpenAPI JSON lives 
        props.setUrl(base + "/v3/api-docs");

        //Keep the configUrl so UI can fetch grouped/config metadata
        props.setConfigUrl(base + "/v3/api-docs/swagger-config");

        //disable the Swagger default URL entirely
        props.setDisableSwaggerDefaultUrl(true);

        LOG.debug("Swagger UI configured with url and configUrl = "+
                 props.getUrl()+", "+props.getConfigUrl());
        return props;
    }

    @Bean
    public SwaggerUiOAuthProperties swaggerUiOAuthProperties() {
        return new SwaggerUiOAuthProperties();
    }
}