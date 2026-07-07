package com.esri.geoportal.base.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:config/hrv.properties")
public class SecurityEndPointPropConfig {

	 @Bean
	    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
	        PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
	       
	        p.setIgnoreResourceNotFound(false);
	        p.setIgnoreUnresolvablePlaceholders(false);
	        return p;
	    } 
	
    @Bean
    public  SecurityEndPointProp securityProperties(Environment env) {
        List<EndpointSecurityConfig> endpoints = new ArrayList<>();

        for (int i = 0; ; i++) {
            String baseKey = "security.secured-endpoints[" + i + "]";
            String pattern = env.getProperty(baseKey + ".pattern");
            String method = env.getProperty(baseKey + ".method");
            String roles = env.getProperty(baseKey + ".roles");

            if (pattern == null && method == null && roles == null) {
                break; // No more entries
            }

            EndpointSecurityConfig endpoint = new EndpointSecurityConfig();
            endpoint.setPattern(pattern);
            endpoint.setMethod(method);
            endpoint.setRoles(roles);
            endpoints.add(endpoint);
        }

        SecurityEndPointProp props = new SecurityEndPointProp();
        props.setSecuredEndpoints(endpoints);
        return props;
    }

}