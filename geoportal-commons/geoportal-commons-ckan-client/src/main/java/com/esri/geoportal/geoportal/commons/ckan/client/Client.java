/*
 * Copyright 2016 Esri, Inc.
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
package com.esri.geoportal.geoportal.commons.ckan.client;

import com.esri.geoportal.commons.constants.HttpConstants;
import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CKAN lightweight client.
 */
public class Client implements Closeable {
  private final Logger LOG = LoggerFactory.getLogger(Client.class);
  
  private static final String PACKAGE_LIST_URL = "/api/3/action/package_search";

  private final CloseableHttpClient httpClient;
  private final URL url;
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Creates instance of the client.
   * @param httpClient HTTP client
   * @param url base URL
   */
  public Client(CloseableHttpClient httpClient, URL url) {
    this.httpClient = httpClient;
    this.url = url;
    
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
  }
  
  /**
   * Lists packages.
   * @param rows number of rows to fetch.
   * @param start start record (0-based)
   * @return response
   * @throws IOException if reading response fails
   * @throws URISyntaxException if invalid URL
   */
  public Response listPackages(long rows, long start) throws IOException, URISyntaxException {
    URI uri = createListPackagesUri(rows, start);
    HttpGet get = new HttpGet(uri);
    get.setConfig(DEFAULT_REQUEST_CONFIG);
    get.setHeader("Content-Type", "application/json; charset=UTF-8");
    get.setHeader("User-Agent", HttpConstants.getUserAgent());
    
    return execute(get, Response.class);
  }
  
  private URI createListPackagesUri(long rows, long start) throws IOException, URISyntaxException {
    return new URIBuilder(url.toURI().resolve(PACKAGE_LIST_URL))
            .addParameter("rows", Long.toString(rows))
            .addParameter("start", Long.toString(start))
            .build();      
  }
  
  private <T> T execute(HttpUriRequest req, Class<T> clazz) throws IOException, URISyntaxException {
    try (CloseableHttpResponse httpResponse = httpClient.execute(req); InputStream contentStream = httpResponse.getEntity().getContent();) {
      String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        T value = null;
        try {
          value = mapper.readValue(responseContent, clazz);
        } catch (Exception ex) {
          throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }
        if (value==null) {
          throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }
        return value;
      }
      
      return mapper.readValue(responseContent, clazz);
    }
  }
}
