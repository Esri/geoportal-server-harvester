package com.esri.geoportal.harvester.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Import({ org.springdoc.core.SpringDocConfiguration.class, 
    org.springdoc.webmvc.core.SpringDocWebMvcConfiguration.class,
    org.springdoc.webmvc.ui.SwaggerConfig.class, 
    org.springdoc.core.SwaggerUiConfigProperties.class,
    org.springdoc.core.SwaggerUiOAuthProperties.class,
    org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class,
    OpenAPIConfig.class
})


@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.esri.geoportal.harvester.rest", "org.springdoc"}) // Add org.springdoc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/5.x.x/"); // Adjust version
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
