package com.esri.geoportal.harvester.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Harvester API")
                        .version("4.0.0")
                        .description("API Documentation for Harvester"))

                // ✅ GLOBAL SECURITY (this enables Authorize button)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste JWT token like: Bearer <token>")))

                // ✅ Apply to ALL endpoints
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))

                // ✅ Add tag for OAuth section
                .addTagsItem(new Tag().name("OAuth2").description("Authorization APIs"));

        // ✅ Add /oauth2/token endpoint manually
        addOAuth2TokenEndpoint(openAPI);

        return openAPI;
    }

    @SuppressWarnings("unchecked")
	private void addOAuth2TokenEndpoint(OpenAPI openAPI) {

        Operation operation = new Operation()
                .summary("Generate OAuth2 JWT Token")
                .description("Generate JWT using client credentials")
                .addTagsItem("OAuth2");

        // ✅ FORM DATA request
        RequestBody requestBody = new RequestBody()
                .required(true)
                .content(new Content()
                        .addMediaType("application/x-www-form-urlencoded",
                                new MediaType().schema(new Schema<>()
                                        .type("object")
                                        .addProperties("grant_type",
                                                new StringSchema().example("client_credentials"))
                                        .addProperties("client_id",
                                                new StringSchema().example("test"))
                                        .addProperties("client_secret",
                                                new StringSchema().example("test"))
                                        .addProperties("scope",
                                                new StringSchema().example("api.read api.write"))
                                        .required(java.util.List.of("grant_type", "client_id", "client_secret"))
                                )));

        operation.setRequestBody(requestBody);

        // ✅ RESPONSE
        Schema<?> responseSchema = new Schema<>()
                .type("object")
                .addProperties("access_token", new StringSchema())
                .addProperties("token_type", new StringSchema().example("Bearer"))
                .addProperties("expires_in", new IntegerSchema().example(3600))
                .addProperties("scope", new StringSchema());

        ApiResponses responses = new ApiResponses()
                .addApiResponse("200", new ApiResponse()
                        .description("Token generated")
                        .content(new Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(responseSchema))))
                .addApiResponse("401", new ApiResponse().description("Invalid client"));

        operation.setResponses(responses);

        // ❗ IMPORTANT: Do NOT apply bearerAuth to token endpoint
        operation.setSecurity(java.util.List.of());

        openAPI.path("/oauth2/token", new PathItem().post(operation));
    }
}