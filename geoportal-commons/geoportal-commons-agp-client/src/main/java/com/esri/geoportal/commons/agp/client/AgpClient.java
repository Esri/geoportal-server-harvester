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
package com.esri.geoportal.commons.agp.client;

import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * ArcGIS Portal client.
 */
public class AgpClient implements Closeable {
  private static final String QUERY_EXTRAS = "-type:\"Layer\" -type: \"Map Document\" -type:\"Map Package\" -type:\"Basemap Package\" -type:\"Mobile Basemap Package\" -type:\"Mobile Map Package\" -type:\"ArcPad Package\" -type:\"Project Package\" -type:\"Project Template\" -type:\"Desktop Style\" -type:\"Pro Map\" -type:\"Layout\" -type:\"Explorer Map\" -type:\"Globe Document\" -type:\"Scene Document\" -type:\"Published Map\" -type:\"Map Template\" -type:\"Windows Mobile Package\" -type:\"Layer Package\" -type:\"Explorer Layer\" -type:\"Geoprocessing Package\" -type:\"Desktop Application Template\" -type:\"Code Sample\" -type:\"Geoprocessing Package\" -type:\"Geoprocessing Sample\" -type:\"Locator Package\" -type:\"Workflow Manager Package\" -type:\"Windows Mobile Package\" -type:\"Explorer Add In\" -type:\"Desktop Add In\" -type:\"File Geodatabase\" -type:\"Feature Collection Template\" -type:\"Code Attachment\" -type:\"Featured Items\" -type:\"Symbol Set\" -type:\"Color Set\" -type:\"Windows Viewer Add In\" -type:\"Windows Viewer Configuration\"";
  
  private final URL rootUrl;
  private final CloseableHttpClient httpClient;
  
  /**
   * Creates instance of the client.
   * @param rootUrl root URL
   */
  public AgpClient(URL rootUrl) {
    this.rootUrl = adjustUrl(rootUrl);
    this.httpClient = HttpClients.createDefault();
  }

  @Override
  public void close() throws IOException {
    httpClient.close();
  }

  /**
   * Adds item.
   * @param username user name
   * @param folderId folder id
   * @param title title
   * @param description description
   * @param text text
   * @param itemType item type (must be a URL type)
   * @param typeKeywords type keywords
   * @param token token
   * @return add item response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ItemResponse addItem(String username, String folderId, String title, String description, String text, ItemType itemType, String [] typeKeywords, String token) throws IOException, URISyntaxException {
    URIBuilder builder = new URIBuilder(addItemUri(username, StringUtils.trimToNull(folderId)));
    
    HttpPost req = new HttpPost(builder.build());
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    params.put("title", title);
    params.put("description", description);
    params.put("type", itemType.getTypeName());
    params.put("text", text);
    params.put("typeKeywords", typeKeywords!=null? Arrays.asList(typeKeywords).stream().collect(Collectors.joining(",")):"");
    params.put("token", token);
    
    req.setEntity(createEntity(params));

    return execute(req,ItemResponse.class);
  }

  /**
   * Adds item.
   * @param username user name
   * @param folderId folder id
   * @param title title
   * @param description description
   * @param url URL
   * @param itemType item type (must be a URL type)
   * @param typeKeywords type keywords
   * @param token token
   * @return add item response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ItemResponse addItem(String username, String folderId, String title, String description, URL url, ItemType itemType, String [] typeKeywords, String token) throws IOException, URISyntaxException {
    URIBuilder builder = new URIBuilder(addItemUri(username, StringUtils.trimToNull(folderId)));
    
    HttpPost req = new HttpPost(builder.build());
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    params.put("title", title);
    params.put("description", description);
    params.put("type", itemType.getTypeName());
    params.put("url", url.toExternalForm());
    params.put("typeKeywords", typeKeywords!=null? Arrays.asList(typeKeywords).stream().collect(Collectors.joining(",")):"");
    params.put("token", token);
    
    req.setEntity(createEntity(params));

    return execute(req,ItemResponse.class);
  }

  /**
   * Updates item item.
   * @param username user name
   * @param folderId folder id
   * @param itemId item id
   * @param title title
   * @param description description
   * @param text text
   * @param itemType item type (must be a URL type)
   * @param typeKeywords type keywords
   * @param token token
   * @return add item response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ItemResponse updateItem(String username, String folderId, String itemId, String title, String description, String text, ItemType itemType, String [] typeKeywords, String token) throws IOException, URISyntaxException {
    URIBuilder builder = new URIBuilder(updateItemUri(username, StringUtils.trimToNull(folderId), itemId));
    
    HttpPost req = new HttpPost(builder.build());
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    params.put("title", title);
    params.put("description", description);
    params.put("type", itemType.getTypeName());
    params.put("text", text);
    params.put("typeKeywords", typeKeywords!=null? Arrays.asList(typeKeywords).stream().collect(Collectors.joining(",")):"");
    params.put("token", token);
    
    req.setEntity(createEntity(params));

    return execute(req,ItemResponse.class);
  }

  /**
   * Adds item.
   * @param username user name
   * @param folderId folder id
   * @param itemId item id
   * @param title title
   * @param description description
   * @param url URL
   * @param itemType item type (must be a URL type)
   * @param typeKeywords type keywords
   * @param token token
   * @return add item response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ItemResponse updateItem(String username, String folderId, String itemId, String title, String description, URL url, ItemType itemType, String [] typeKeywords, String token) throws IOException, URISyntaxException {
    URIBuilder builder = new URIBuilder(updateItemUri(username, StringUtils.trimToNull(folderId), itemId));
    
    HttpPost req = new HttpPost(builder.build());
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    params.put("title", title);
    params.put("description", description);
    params.put("type", itemType.getTypeName());
    params.put("url", url.toExternalForm());
    params.put("typeKeywords", typeKeywords!=null? Arrays.asList(typeKeywords).stream().collect(Collectors.joining(",")):"");
    params.put("token", token);
    
    req.setEntity(createEntity(params));

    return execute(req,ItemResponse.class);
  }
  
  /**
   * Sharing item.
   * @param username user name
   * @param folderId folder id
   * @param itemId item id
   * @param everyone <code>true</code> to share with everyone
   * @param org <code>true</code> to share with group
   * @param groups list of groups to share with
   * @param token token
   * @return share response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ShareResponse share(String username, String folderId, String itemId, boolean everyone, boolean org, String [] groups, String token)  throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(shareUri(username, folderId, itemId));
    
    HttpPost req = new HttpPost(builder.build());
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    params.put("everyone", Boolean.toString(everyone));
    params.put("org", Boolean.toString(org));
    params.put("groups", groups!=null? Arrays.asList(groups).stream().collect(Collectors.joining(",")): "");
    params.put("token", token);
    
    req.setEntity(createEntity(params));

    return execute(req,ShareResponse.class);
  }
  
  /**
   * Deletes item.
   * @param username user name
   * @param folderId folder id
   * @param itemId item id
   * @param token token
   * @return delete response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public DeleteResponse delete(String username, String folderId, String itemId, String token)  throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(deleteUri(username, folderId, itemId));
    
    builder.setParameter("f", "json");
    builder.setParameter("token", token);
    
    HttpPost req = new HttpPost(builder.build());

    return execute(req,DeleteResponse.class);
  }
  
  /**
   * Lists content.
   * @param username user name
   * @param folder folder (optional)
   * @param num number items to return
   * @param start start item
   * @param token token (optional)
   * @return content response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ContentResponse listContent(String username, String folder, long num, long start, String token) throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(userUri(username, folder));
    
    builder.setParameter("f", "json");
    if (token!=null) {
      builder.setParameter("token", token);
    }
    HttpGet req = new HttpGet(builder.build());
    
    return execute(req,ContentResponse.class);
  }
  
  /**
   * Searches for items.
   * @param query query
   * @param num max number of items
   * @param start start item
   * @param token token (optional)
   * @return query response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public QueryResponse search(String query, long num, long start, String token) throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(searchUri());
    
    builder.setParameter("f", "json");
    builder.setParameter("q", String.format("%s %s", query, QUERY_EXTRAS));
    if (token!=null) {
      builder.setParameter("token", token);
    }
    HttpGet req = new HttpGet(builder.build());
    
    return execute(req,QueryResponse.class);
  }
  
  /**
   * Generates token.
   *
   * @param minutes expiration in minutes.
   * @param credentials credentials.
   * @return token response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if accessing token fails
   */
  public TokenResponse generateToken(int minutes, SimpleCredentials credentials) throws URISyntaxException, IOException {
    HttpPost req = new HttpPost(generateTokenUri());
    
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    if (credentials != null) {
      params.put("username", StringUtils.trimToEmpty(credentials.getUserName()));
      params.put("password", StringUtils.trimToEmpty(credentials.getPassword()));
    }
    params.put("client", "referer");
    params.put("referer", InetAddress.getLocalHost().getHostAddress());
    params.put("expiration", Integer.toString(minutes));
    
    req.setEntity(createEntity(params));

    return execute(req,TokenResponse.class);
  }
  
  private URI updateItemUri(String username, String folderId, String itemId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + username + (folderId!=null? "/" +folderId: "") +"/items/" + itemId + "/update");
    return builder.build();
  }

  private URI addItemUri(String username, String folderId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + username + (folderId!=null? "/" +folderId: "") +"/addItem");
    return builder.build();
  }
  
  private URI shareUri(String username, String folderId, String itemId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + username + (folderId!=null? "/" +folderId: "") +"/items/" + itemId + "/share");
    return builder.build();
  }
  
  private URI deleteUri(String username, String folderId, String itemId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + username + (folderId!=null? "/" +folderId: "") +"/items/" + itemId + "/delete");
    return builder.build();
  }
  
  private URI userUri(String username, String folderId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + username + (folderId!=null? "/"+folderId: ""));
    return builder.build();
  }
  
  private URI searchUri() throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/search");
    return builder.build();
  }
  
  private URI generateTokenUri() throws URISyntaxException {
    return rootUrl.toURI().resolve("sharing/generateToken");
  }
  
  private HttpEntity createEntity(Map<String, String> params) throws UnsupportedEncodingException {
    return  new UrlEncodedFormEntity(params.entrySet().stream()
            .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList()));
  }
  
  private <T> T execute(HttpUriRequest req, Class<T> clazz) throws IOException {

    try (CloseableHttpResponse httpResponse = httpClient.execute(req); InputStream contentStream = httpResponse.getEntity().getContent();) {
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      return mapper.readValue(responseContent, clazz);
    }
  }

  private URL adjustUrl(URL rootUrl) {
    try {
      return new URL(rootUrl.toExternalForm().replaceAll("/*$", "/"));
    } catch (MalformedURLException ex) {
      return rootUrl;
    }
  }
}
