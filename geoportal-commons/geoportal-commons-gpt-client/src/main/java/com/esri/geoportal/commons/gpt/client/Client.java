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

import com.esri.geoportal.commons.constants.HttpConstants;
import com.esri.geoportal.commons.gpt.client.QueryResponse.Hit;
import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GPT 2.0 Client.
 */
public class Client implements Closeable {
  private final Logger LOG = LoggerFactory.getLogger(Client.class);

  private static final String REST_ITEM_URL = "rest/metadata/item";
  private static final String ELASTIC_SEARCH_URL = "elastic/metadata/item/_search";
  private static final String ELASTIC_SCROLL_URL = "elastic/_search/scroll";
  private static final String TOKEN_URL = "oauth/token";

  private final CloseableHttpClient httpClient;
  private final URL url;
  private final SimpleCredentials cred;
  
  private TokenInfo tokenInfo;

  /**
   * Creates instance of the client.
   *
   * @param httpClient HTTP client
   * @param url URL of the GPT REST end point
   * @param cred credentials
   */
  public Client(CloseableHttpClient httpClient, URL url, SimpleCredentials cred) {
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
    StringEntity entity = new StringEntity(json,"UTF-8");

    List<String> ids = !forceAdd ? queryIds(data.src_uri_s) : Collections.emptyList();
    
    URI pubUri = !ids.isEmpty()? createItemUri(ids.get(0)): createItemsUri();
    try {
      return publish(pubUri, entity);
    } catch(HttpResponseException ex) {
      if (ex.getStatusCode()==401) {
        clearToken();
        pubUri = !ids.isEmpty()? createItemUri(ids.get(0)): createItemsUri();
        return publish(pubUri, entity);
      } else {
        throw ex;
      }
    }
  }
  
  private PublishResponse publish(URI uri, StringEntity entity)  throws IOException, URISyntaxException {
    HttpPut put = new HttpPut(uri);
    put.setConfig(DEFAULT_REQUEST_CONFIG);
    put.setEntity(entity);
    put.setHeader("Content-Type", "application/json; charset=UTF-8");
    put.setHeader("User-Agent", HttpConstants.getUserAgent());

    return execute(put,PublishResponse.class);
  }
  
  /**
   * Reads metadata.
   * @param id id of the metadata
   * @return string representing metadata
   * @throws URISyntaxException if invalid URI
   * @throws IOException if reading metadata fails
   */
  public String readXml(String id) throws URISyntaxException, IOException {
    URI xmlUri = createXmlUri(id);
    try {
      return readXml(xmlUri);
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode()==401) {
        clearToken();
        xmlUri = createXmlUri(id);
        return readXml(xmlUri);
      } else {
        throw ex;
      }
    }
  }
  
  private String readXml(URI uri) throws URISyntaxException, IOException {
    HttpGet get = new HttpGet(uri);
    get.setConfig(DEFAULT_REQUEST_CONFIG);
    get.setHeader("User-Agent", HttpConstants.getUserAgent());
    
    try (CloseableHttpResponse httpResponse = httpClient.execute(get); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      return responseContent;
    }
  }
  
  /**
   * Reads metadata.
   * @param id id of the metadata
   * @return string representing metadata
   * @throws URISyntaxException if invalid URI
   * @throws IOException if reading metadata fails
   */
  public EntryRef readItem(String id) throws URISyntaxException, IOException {
    URI itemUri = createItemUri(id);
    try {
      return readItem(itemUri);
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode()==401) {
        clearToken();
        itemUri = createItemUri(id);
        return readItem(itemUri);
      } else {
        throw ex;
      }
    }
  }
  
  private EntryRef readItem(URI uri) throws URISyntaxException, IOException {
    HttpGet get = new HttpGet(uri);
    get.setConfig(DEFAULT_REQUEST_CONFIG);
    get.setHeader("User-Agent", HttpConstants.getUserAgent());
    Hit hit =  execute(get,Hit.class);
    return new EntryRef(hit._id, readUri(hit._source), readLastUpdated(hit._source));
  }
  
  /**
   * Returns listIds of ids.
   * @return listIds of ids or <code>null</code> if no more ids.
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public List<String> listIds()  throws URISyntaxException, IOException {
    return queryIds(null, null, 200);
  }
  
  private URI readUri(QueryResponse.Source source) {
    if (source!=null && source.src_uri_s!=null) {
      try {
        return new URI(source.src_uri_s);
      } catch (Exception ex) {}
    }
    return null;
  }
  
  private Date readLastUpdated(QueryResponse.Source source) {
    if (source!=null && source.src_lastupdate_dt!=null) {
      try {
        return Date.from(ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(source.src_lastupdate_dt)).toInstant());
      } catch (Exception ex) {}
    }
    return null;
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
    URI deleteUri = createItemUri(id);
    try {
      return delete(deleteUri);
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode()==401) {
        clearToken();
        deleteUri = createItemUri(id);
        return delete(deleteUri);
      } else {
        throw ex;
      }
    }
  }
  
  private PublishResponse delete(URI uri) throws URISyntaxException, IOException {
    HttpDelete del = new HttpDelete(uri);
    del.setConfig(DEFAULT_REQUEST_CONFIG);
    del.setHeader("User-Agent", HttpConstants.getUserAgent());
    return execute(del, PublishResponse.class);
  }
  
  private URI createItemsUri() throws URISyntaxException, IOException {
    return new URIBuilder(url.toURI().resolve(REST_ITEM_URL))
            .addParameter("access_token", getAccessToken())
            .build();
  }
  
  private URI createItemUri(String id) throws URISyntaxException, IOException {
    return new URIBuilder(url.toURI().resolve(REST_ITEM_URL + "/" + id))
            .addParameter("access_token", getAccessToken())
            .build();
  }
  
  private URI createXmlUri(String id) throws URISyntaxException, IOException {
    return new URIBuilder(url.toURI().resolve(REST_ITEM_URL + "/" + id + "/xml"))
            .addParameter("access_token", getAccessToken())
            .build();
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
   * @return listIds of ids
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  private List<String> queryIds(String term, String value, long size) throws IOException, URISyntaxException {
    ArrayList<String> ids = new ArrayList<>();
    SearchContext searchContext = new SearchContext();
    
    while (true) {
      QueryResponse response = query(term, value, size, searchContext);
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
    }
    
    return ids;
  }

  /**
   * Query items.
   *
   * @param term term name
   * @param value value of the term
   * @param size size
   * @return query response
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  private QueryResponse query(String term, String value, long size, SearchContext searchContext) throws IOException, URISyntaxException {
    URI uri = createQueryUri(term, value, size, searchContext);
    try {
      QueryResponse response =  query(uri);
      searchContext._scroll_id = response._scroll_id;
      return response;
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode()==401) {
        clearToken();
        uri = createQueryUri(term, value, size, searchContext);
        QueryResponse response =  query(uri);
        searchContext._scroll_id = response._scroll_id;
        return response;
      } else {
        throw ex;
      }
    }
  }
  
  private void clearToken() {
    tokenInfo = null;
  }
  
  private QueryResponse query(URI uri)  throws IOException, URISyntaxException {
    HttpGet get = new HttpGet(uri);
    get.setConfig(DEFAULT_REQUEST_CONFIG);
    get.setHeader("Content-Type", "application/json");
    get.setHeader("User-Agent", HttpConstants.getUserAgent());
    
    return  execute(get, QueryResponse.class);
  }
  
  private URI createQueryUri(String term, String value, long size, SearchContext searchContext) throws IOException, URISyntaxException {
    if (searchContext._scroll_id==null) {
      return new URIBuilder(url.toURI().resolve(ELASTIC_SEARCH_URL))
              .addParameter("q",term!=null && value!=null? String.format("%s:\"%s\"", term, value): "*:*")
              .addParameter("size", Long.toString(size))
              .addParameter("scroll", "1m")
              .addParameter("access_token", getAccessToken())
              .build();      
    } else {
      return new URIBuilder(url.toURI().resolve(ELASTIC_SCROLL_URL))
              .addParameter("scroll_id", searchContext._scroll_id)
              .addParameter("scroll", "1m")
              .addParameter("access_token", getAccessToken())
              .build();
    }
  }
  
  private <T> T execute(HttpUriRequest req, Class<T> clazz) throws IOException, URISyntaxException {

    try (CloseableHttpResponse httpResponse = httpClient.execute(req); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      
      String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      return mapper.readValue(responseContent, clazz);
    }
  }
  
  private String getAccessToken() throws URISyntaxException, IOException {
    LocalDateTime now = LocalDateTime.now();
    if (tokenInfo==null || tokenInfo.validTill.minusMinutes(2).isBefore(now)) {
      Token token = generateToken();
      TokenInfo ti = new TokenInfo();
      ti.token = token;
      ti.validTill = now.plusMinutes(token.expires_in);
      tokenInfo = ti;
    }
    return tokenInfo.token.access_token;
  }
  
  private Token generateToken() throws URISyntaxException, UnsupportedEncodingException, IOException {
    HttpPost post = new HttpPost(url.toURI().resolve(TOKEN_URL));
    post.setConfig(DEFAULT_REQUEST_CONFIG);
    post.setHeader("User-Agent", HttpConstants.getUserAgent());
    post.setHeader("Content-Type","application/x-www-form-urlencoded");
    post.setHeader("Accept","application/json");
    HashMap<String, String> params = new HashMap<>();
    if (cred != null) {
      params.put("username", StringUtils.trimToEmpty(cred.getUserName()));
      params.put("password", StringUtils.trimToEmpty(cred.getPassword()));
    }
    params.put("grant_type", "password");
    params.put("client_id", "geoportal-client");
    HttpEntity entity = new UrlEncodedFormEntity(params.entrySet().stream()
            .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList()));
    post.setEntity(entity);
    
    return execute(post,Token.class);
  }

  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
  }
  
  public static class SearchContext {
    public String _scroll_id;
  }
  
  public static class Token {
    public String access_token;
    public String token_type;
    public Long expires_in;
    public String scope;
    public String jti;
  }
  
  private static class TokenInfo {
    public Token token;
    public LocalDateTime validTill;
  }
}
