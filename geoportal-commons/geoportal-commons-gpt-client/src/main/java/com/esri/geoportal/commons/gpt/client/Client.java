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

import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;

/**
 * GPT 2.0 Client.
 */
public class Client implements Closeable {
  private final HttpClient httpClient;
  private final URL url;
  private final String userName;
  private final String password;
  
  /**
   * Creates instance of the client.
   * @param httpClient HTTP client
   * @param url URL of the GPT REST end point
   * @param userName user name
   * @param password password
   */
  public Client(HttpClient httpClient, URL url, String userName, String password) {
    this.httpClient = httpClient;
    this.url = url;
    this.userName = userName;
    this.password = password;
  }

  /**
   * Creates instance of the client.
   * @param url URL of the GPT REST end point
   * @param userName user name
   * @param password password
   */
  public Client(URL url, String userName, String password) {
    this(HttpClients.createDefault(), url, userName, password);
  }
  
  /**
   * Publishes a document.
   * @param document document to publish
   * @return response information
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public Response publish(String document) throws IOException, URISyntaxException {
    HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(
            new AuthScope(targetHost.getHostName(), targetHost.getPort()),
            new UsernamePasswordCredentials(userName, password));    
    
    // Create AuthCache instance
    AuthCache authCache = new BasicAuthCache();
    // Generate BASIC scheme object and add it to the local auth cache
    BasicScheme basicAuth = new BasicScheme();
    authCache.put(targetHost, basicAuth);

    // Add AuthCache to the execution context
    HttpClientContext context = HttpClientContext.create();
    context.setCredentialsProvider(credsProvider);
    context.setAuthCache(authCache);

    StringEntity entity = new StringEntity(document);
    HttpPut put = new HttpPut(url.toURI().resolve("rest/metadata/item"));
    put.setConfig(DEFAULT_REQUEST_CONFIG);
    put.setEntity(entity);
    put.setHeader("Content-Type", "application/xml");
    
    HttpResponse httpResponse = httpClient.execute(put,context);
    String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
    
    try (InputStream contentStream = httpResponse.getEntity().getContent();) {
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      System.out.println(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      ObjectMapper mapper = new ObjectMapper();
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
