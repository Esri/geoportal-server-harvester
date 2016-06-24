/*
 * Copyright 2016 Esri, Inc..
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
package com.esri.geoportal.commons.gpt.client;

import static com.esri.geoportal.commons.utils.HttpClientContextBuilder.createHttpClientContext;
import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

/**
 * GPT 2.0 Client.
 */
public class Client implements Closeable {
  private final HttpClient httpClient;
  private final URL url;
  private final SimpleCredentials cred;
  
  /**
   * Creates instance of the client.
   * @param httpClient HTTP client
   * @param url URL of the GPT REST end point
   * @param cred credentials
   */
  public Client(HttpClient httpClient, URL url, SimpleCredentials cred) {
    this.httpClient = httpClient;
    this.url = url;
    this.cred = cred;
  }

  /**
   * Creates instance of the client.
   * @param url URL of the GPT REST end point
   * @param cred credentials
   */
  public Client(URL url, SimpleCredentials cred) {
    this(HttpClients.createDefault(), url, cred);
  }
  
  /**
   * Publishes a document.
   * @param data data to publish
   * @return response information
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public Response publish(Data data) throws IOException, URISyntaxException {
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(data);

    StringEntity entity = new StringEntity(json);
    HttpPut put = new HttpPut(url.toURI().resolve("rest/metadata/item"));
    put.setConfig(DEFAULT_REQUEST_CONFIG);
    put.setEntity(entity);
    put.setHeader("Content-Type", "application/json");
    
    HttpClientContext context = createHttpClientContext(url, cred);
    HttpResponse httpResponse = httpClient.execute(put,context);
    String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
    
    try (InputStream contentStream = httpResponse.getEntity().getContent();) {
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      System.out.println(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      return mapper.readValue(responseContent, Response.class);
    }
  }
  
  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable)httpClient).close();
    }
  }
}
