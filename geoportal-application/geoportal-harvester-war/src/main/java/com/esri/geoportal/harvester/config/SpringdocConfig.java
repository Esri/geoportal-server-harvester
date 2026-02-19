package com.esri.geoportal.harvester.config;

import jakarta.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;

import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Bridge beans that Spring Boot would normally provide.
 * Required by springdoc-openapi v2 when running without Spring Boot.
 */
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
		//String url = (ctx == null || ctx.isEmpty() ? "" : ctx) + "/v3/api-docs";
		String base = (ctx == null || ctx.isEmpty() ? "" : ctx);
		// ❗ This sets the initial URL Swagger UI loads
		LOG.info("SwaggerUiConfigProperties url "+base);

		props.setConfigUrl(base + "/v3/api-docs/swagger-config");

		return props;
	}

	@Bean
	public SwaggerUiOAuthProperties swaggerUiOAuthProperties() {
		return new SwaggerUiOAuthProperties();
	}

}