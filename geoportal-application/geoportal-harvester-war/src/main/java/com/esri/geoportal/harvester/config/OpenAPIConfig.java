package com.esri.geoportal.harvester.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
	public class OpenAPIConfig {
//    @Bean
//    public GroupedOpenApi publicApi() {
//        return GroupedOpenApi.builder()
//                .group("public") // You can choose any group name
//                .pathsToMatch("/rest/**") // Specify paths to include in this group
//                .build();
//    }
	    @Bean
	    public OpenAPI customOpenAPI() {
	        return new OpenAPI()
	                .info(new Info().title("Harvester API")
	                                .version("3.0.0")
	                                .description("API Documentation for Harvester"));
	    }
	    

	}

