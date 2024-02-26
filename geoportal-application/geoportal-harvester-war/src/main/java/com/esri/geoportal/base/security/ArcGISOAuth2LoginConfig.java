/*
 * Copyright 2024 cont_anki.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.base.security;


import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author cont_anki
 */
@Configuration
public class ArcGISOAuth2LoginConfig {
    
  
     @Bean
     public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> arcgisTokenResponseClient() {
		OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
				new OAuth2AccessTokenResponseHttpMessageConverter();
		tokenResponseHttpMessageConverter.setTokenResponseConverter(new CustomAccessTokenResponseConverter());

		RestTemplate restTemplate = new RestTemplate(Arrays.asList(
				new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

		DefaultAuthorizationCodeTokenResponseClient tokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
		tokenResponseClient.setRestOperations(restTemplate);

		return tokenResponseClient;
	}
     
     @Bean
     public DefaultOAuth2UserService customUserService()
     {
         DefaultOAuth2UserService userService = new DefaultOAuth2UserService();
         
         userService.setRequestEntityConverter(new CustomRequestEntityConverter());
         
         return userService;
         
     }
     
     
}

    


