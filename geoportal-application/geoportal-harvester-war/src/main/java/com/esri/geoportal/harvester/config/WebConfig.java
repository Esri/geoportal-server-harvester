package com.esri.geoportal.harvester.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;

@Import({
    // Springdoc core + MVC
    org.springdoc.core.configuration.SpringDocConfiguration.class,
    org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class,

 // Springdoc-managed Swagger UI controller  
    org.springdoc.webmvc.ui.SwaggerConfig.class,
    OpenAPIConfig.class,
    SpringdocConfig.class
})
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.esri.geoportal.harvester.rest"})
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {       
        registry.addViewController("/").setViewName("forward:/index.html");

        // Convenience redirects for users typing shorter paths
        registry.addRedirectViewController("/swagger-ui", "/swagger-ui/");
        registry.addRedirectViewController("/swagger-ui/", "/swagger-ui/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve Swagger UI (WebJar) with version-less path (locator resolves to 5.31.0)
//        registry.addResourceHandler("/swagger-ui/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/5.31.0/");
        // Serve other WebJars assets
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // Allow container default servlet to serve /index.html and other static files if present
        configurer.enable();
    }
}