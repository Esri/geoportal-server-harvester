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
package com.esri.geoportal.commons.gpt.client;

import static com.esri.geoportal.commons.utils.HttpClientContextBuilder.createHttpClientContext;
import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GPT 2.0 Client.
 */
public class Client implements Closeable {
  private final Logger LOG = LoggerFactory.getLogger(Client.class);

  private static final String REST_ITEM_URL = "rest/metadata/item";
  private static final String REST_SEARCH_URL = "rest/metadata/search";

  private final HttpClient httpClient;
  private final URL url;
  private final SimpleCredentials cred;

  /**
   * Creates instance of the client.
   *
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
   *
   * @param url URL of the GPT REST end point
   * @param cred credentials
   */
  public Client(URL url, SimpleCredentials cred) {
    this(HttpClients.createDefault(), url, cred);
  }

  /**
   * Publishes a document.
   *
   * @param data data to publish
   * @param forceAdd <code>true</code> to force add.
   * @return response information
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public PublishResponse publish(PublishRequest data, boolean forceAdd) throws IOException, URISyntaxException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    String json = mapper.writeValueAsString(data);
    StringEntity entity = new StringEntity(json);

    List<String> ids = !forceAdd ? queryIds(data.src_uri_s) : Collections.emptyList();
    HttpRequestBase request;
    switch (ids.size()) {
      case 0: {
        HttpPut put = new HttpPut(url.toURI().resolve(REST_ITEM_URL));
        put.setConfig(DEFAULT_REQUEST_CONFIG);
        put.setEntity(entity);
        put.setHeader("Content-Type", "application/json");
        request = put;
      }
      break;
      case 1: {
        HttpPut put = new HttpPut(url.toURI().resolve(REST_ITEM_URL + "/" + ids.get(0)));
        put.setConfig(DEFAULT_REQUEST_CONFIG);
        put.setEntity(entity);
        put.setHeader("Content-Type", "application/json");
        request = put;
      }
      break;
      default:
        throw new IOException(String.format("Error updating item: %s", data.src_uri_s));
    }

    HttpClientContext context = createHttpClientContext(url, cred);
    HttpResponse httpResponse = httpClient.execute(request, context);
    String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();

    try (InputStream contentStream = httpResponse.getEntity().getContent();) {
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      return mapper.readValue(responseContent, PublishResponse.class);
    }
  }

  /**
   * Query items by src_source_uri_s.
   *
   * @param src_source_uri_s query
   * @return query response
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public List<String> queryBySource(String src_source_uri_s) throws IOException, URISyntaxException {
    return queryIds("src_source_uri_s", src_source_uri_s, 200);
  }

  /**
   * Deletes record by id.
   * @param id record id
   * @return publish response
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public PublishResponse delete(String id) throws URISyntaxException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    HttpDelete del = new HttpDelete(url.toURI().resolve(REST_ITEM_URL + "/" + id));
    del.setConfig(DEFAULT_REQUEST_CONFIG);
    HttpClientContext context = createHttpClientContext(url, cred);
    HttpResponse httpResponse = httpClient.execute(del, context);
    String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();

    try (InputStream contentStream = httpResponse.getEntity().getContent();) {
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      return mapper.readValue(responseContent, PublishResponse.class);
    }
  }

  /**
   * Query items by src_uri_s.
   *
   * @param src_uri_s query
   * @return query response
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  private List<String> queryIds(String src_uri_s) throws IOException, URISyntaxException {
    return queryIds("src_uri_s", src_uri_s, 5);
  }
  
  /**
   * Query ids.
   * @param term term to query
   * @param value value of the term
   * @param size size of the chunk
   * @return list of ids
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  private List<String> queryIds(String term, String value, long size) throws IOException, URISyntaxException {
    ArrayList<String> ids = new ArrayList<>();
    
    long from = 0;
    
    while (true) {
      QueryResponse response = query(term, value, from, size);
      if (Thread.currentThread().isInterrupted()) {
        break;
      }
      if (response.hits == null || response.hits.hits == null || response.hits.hits.isEmpty()) {
        break;
      }
      ids.addAll(response.hits.hits.stream()
              .map(h -> h._id)
              .filter(id -> id != null)
              .collect(Collectors.toList()));
      from += response.hits.hits.size();
    }
    
    return ids;
  }

  /**
   * Query items.
   *
   * @param term term name
   * @param value value of the term
   * @param from starting record
   * @param size number of records to return
   * @return query response
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  private QueryResponse query(String term, String value, long from, long size) throws IOException, URISyntaxException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    HttpGet get = new HttpGet(url.toURI().resolve(REST_SEARCH_URL).toASCIIString() + 
            String.format("?q=%s:%s&from=%d&size=%d", term, URLEncoder.encode("\"" + value + "\"", "UTF-8"), from, size));
    get.setConfig(DEFAULT_REQUEST_CONFIG);
    get.setHeader("Content-Type", "application/json");

    HttpClientContext context = createHttpClientContext(url, cred);
    HttpResponse httpResponse = httpClient.execute(get, context);
    String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();

    try (InputStream contentStream = httpResponse.getEntity().getContent();) {
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      QueryResponse queryResponse = mapper.readValue(responseContent, QueryResponse.class);
      return queryResponse;
    }
  }

  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
  }
}
