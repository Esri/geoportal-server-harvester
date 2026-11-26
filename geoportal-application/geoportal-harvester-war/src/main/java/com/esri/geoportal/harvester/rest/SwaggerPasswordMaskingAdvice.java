/*
 * Copyright 2026 Esri, Inc.
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
package com.esri.geoportal.harvester.rest;

import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_PASSWORD;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Masks credential passwords (<code>cred-password</code>) as <code>***</code>
 * in JSON responses for requests made from Swagger UI.
 * <p>
 * Requests from the harvester web UI and other clients are not changed. They
 * still get the scrambled password, which they need to read it back correctly.
 */
@ControllerAdvice
public class SwaggerPasswordMaskingAdvice implements ResponseBodyAdvice<Object> {

  private static final Logger LOG = LoggerFactory.getLogger(SwaggerPasswordMaskingAdvice.class);
  private static final String MASK = "***";
  private static final String SWAGGER_UI_PATH = "/swagger-ui/";

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
          Class<? extends HttpMessageConverter<?>> selectedConverterType,
          ServerHttpRequest request, ServerHttpResponse response) {
    if (body == null || !isSwaggerRequest(request)) {
      return body;
    }
    try {
      JsonNode tree = mapper.valueToTree(body);
      return mask(tree) ? tree : body;
    } catch (IllegalArgumentException ex) {
      LOG.debug("Unable to mask passwords in response body.", ex);
      return body;
    }
  }

  private boolean isSwaggerRequest(ServerHttpRequest request) {
    List<String> referers = request.getHeaders().get(HttpHeaders.REFERER);
    return referers != null && referers.stream().anyMatch(r -> r != null && r.contains(SWAGGER_UI_PATH));
  }

  /**
   * Recursively replaces non-empty password values with the mask.
   * @param node JSON node
   * @return <code>true</code> if anything was masked
   */
  private boolean mask(JsonNode node) {
    boolean masked = false;
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      Iterator<Map.Entry<String, JsonNode>> fields = obj.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        JsonNode value = field.getValue();
        if (P_CRED_PASSWORD.equals(field.getKey()) && value.isTextual() && !value.asText().isEmpty()) {
          field.setValue(obj.textNode(MASK));
          masked = true;
        } else {
          masked |= mask(value);
        }
      }
    } else if (node.isArray()) {
      for (JsonNode child : node) {
        masked |= mask(child);
      }
    }
    return masked;
  }
}
