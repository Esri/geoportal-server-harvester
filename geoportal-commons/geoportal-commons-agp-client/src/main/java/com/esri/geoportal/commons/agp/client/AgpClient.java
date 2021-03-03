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

import com.esri.geoportal.commons.constants.ItemType;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ArcGIS Portal client.
 */
public class AgpClient implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(AgpClient.class);
  private static final String QUERY_EXTRAS = "-type:\"Layer\" -type: \"Map Document\" -type:\"Map Package\" -type:\"Basemap Package\" -type:\"Mobile Basemap Package\" -type:\"Mobile Map Package\" -type:\"ArcPad Package\" -type:\"Project Package\" -type:\"Project Template\" -type:\"Desktop Style\" -type:\"Pro Map\" -type:\"Layout\" -type:\"Explorer Map\" -type:\"Globe Document\" -type:\"Scene Document\" -type:\"Published Map\" -type:\"Map Template\" -type:\"Windows Mobile Package\" -type:\"Layer Package\" -type:\"Explorer Layer\" -type:\"Geoprocessing Package\" -type:\"Desktop Application Template\" -type:\"Code Sample\" -type:\"Geoprocessing Package\" -type:\"Geoprocessing Sample\" -type:\"Locator Package\" -type:\"Workflow Manager Package\" -type:\"Windows Mobile Package\" -type:\"Explorer Add In\" -type:\"Desktop Add In\" -type:\"File Geodatabase\" -type:\"Feature Collection Template\" -type:\"Code Attachment\" -type:\"Featured Items\" -type:\"Symbol Set\" -type:\"Color Set\" -type:\"Windows Viewer Add In\" -type:\"Windows Viewer Configuration\"";
  private static final Integer DEFAULT_MAX_REDIRECTS = 5;
  
  private final URL rootUrl;
  private final SimpleCredentials credentials;
  private final CloseableHttpClient httpClient;
  private final Integer maxRedirects;
  
  /**
   * Creates instance of the client.
   * @param httpClient HTTP client
   * @param rootUrl root URL
   * @param credentials credentials
   */
  public AgpClient(CloseableHttpClient httpClient, URL rootUrl, SimpleCredentials credentials) {
    this(httpClient, rootUrl, credentials, DEFAULT_MAX_REDIRECTS);
  }

  public AgpClient(CloseableHttpClient httpClient, URL rootUrl, SimpleCredentials credentials, Integer maxRedirects) {
    this.rootUrl = adjustUrl(rootUrl);
    this.credentials = credentials;
    this.httpClient = httpClient;
    this.maxRedirects = maxRedirects;
  }

  @Override
  public void close() throws IOException {
    httpClient.close();
  }

  /**
   * Adds item.
   * @param owner user name
   * @param folderId folder id (optional)
   * @param title title
   * @param description description
   * @param url URL
   * @param thumbnailUrl thumbnail url
   * @param itemType item type (must be a URL type)
   * @param extent extent
   * @param typeKeywords type keywords
   * @param tags tags tags
   * @param fileToUpload the file to be uploaded
   * @param token token
   * @return add item response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ItemResponse addItem(String owner, String folderId, String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double [] extent, String [] typeKeywords, String [] tags, File fileToUpload, String token) throws IOException, URISyntaxException {
    boolean multipart = fileToUpload!=null;
    URIBuilder builder = new URIBuilder(addItemUri(owner, StringUtils.trimToNull(folderId)));
    
    HttpPost req = new HttpPost(builder.build());
    
    Map<String, String> params = makeStdParams(title, description, itemType, thumbnailUrl, extent, typeKeywords, tags, token);
    if (!multipart) {
      params.put("url", url.toExternalForm());
    }
    
    req.setEntity(multipart? createEntity(params, fileToUpload): createEntity(params));

    return execute(req,ItemResponse.class);
  }
  
  /**
   * Adds item.
   * @param owner user name
   * @param folderId folder id (optional)
   * @param itemId item id
   * @param title title
   * @param description description
   * @param url URL
   * @param thumbnailUrl thumbnail URL
   * @param itemType item type (must be a URL type)
   * @param extent extent
   * @param typeKeywords type keywords
   * @param tags tags tags
   * @param fileToUpload the file to be uploaded
   * @param token token
   * @return add item response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ItemResponse updateItem(String owner, String folderId, String itemId, String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double [] extent, String [] typeKeywords, String [] tags, File fileToUpload, String token) throws IOException, URISyntaxException {
    boolean multipart = fileToUpload!=null;
    URIBuilder builder = new URIBuilder(updateItemUri(owner, StringUtils.trimToNull(folderId), itemId));
    
    HttpPost req = new HttpPost(builder.build());
    
    Map<String, String> params = makeStdParams(title, description, itemType, thumbnailUrl, extent, typeKeywords, tags, token);
    if (!multipart) {
      params.put("url", url.toExternalForm());
    }
    
    req.setEntity(multipart? createEntity(params, fileToUpload): createEntity(params));

    return execute(req,ItemResponse.class);
  }

  /**
   * Reads item information.
   * @param itemId item id
   * @param token token
   * @return item entry
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ItemEntry readItem(String itemId, String token) throws IOException, URISyntaxException {
    URIBuilder builder = new URIBuilder(itemInfoUri(itemId));
    
    builder.setParameter("f", "json");
    if (token!=null) {
      builder.setParameter("token", token);
    }
    HttpGet req = new HttpGet(builder.build());
    
    return execute(req,ItemEntry.class);
  }
  
  /**
   * Reads item metadata.
   * @param itemId item id
   * @param format metadata format
   * @param token token
   * @return item metadata if available
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public String readItemMetadata(String itemId, MetadataFormat format, String token) throws IOException, URISyntaxException {
    URIBuilder builder = new URIBuilder(itemMetaUri(itemId));
    
    builder.setParameter("format", (format != null ? format : MetadataFormat.DEFAULT).toString());
    if (token!=null) {
      builder.setParameter("token", token);
    }
    HttpGet req = new HttpGet(builder.build());
    
    try {
      return execute(req, 0);
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode() == 500) {
        return null;
      }
      throw ex;
    }
  }
  
  /**
   * Sharing item.
   * @param owner user name
   * @param folderId folder id (optional)
   * @param itemId item id
   * @param everyone <code>true</code> to share with everyone
   * @param org <code>true</code> to share with group
   * @param groups list of groups to share with
   * @param token token
   * @return share response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ShareResponse share(String owner, String folderId, String itemId, boolean everyone, boolean org, String [] groups, String token)  throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(shareUri(owner, folderId, itemId));
    
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
   * @param owner owner
   * @param folderId folder id
   * @param itemId item id
   * @param token token
   * @return delete response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public DeleteResponse delete(String owner, String folderId, String itemId, String token)  throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(deleteUri(owner, folderId, itemId));
    
    builder.setParameter("f", "json");
    builder.setParameter("token", token);
    
    HttpPost req = new HttpPost(builder.build());

    return execute(req,DeleteResponse.class);
  }
  
  /**
   * Lists content.
   * @param owner owner
   * @param folderId folder id (optional)
   * @param num number items to return
   * @param start start item
   * @param token token (optional)
   * @return content response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ContentResponse listContent(String owner, String folderId, long num, long start, String token) throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(userUri(owner, folderId));
    
    builder.setParameter("f", "json");
    builder.setParameter("num", Long.toString(num));
    builder.setParameter("start", Long.toString(start));
    if (token!=null) {
      builder.setParameter("token", token);
    }
    HttpGet req = new HttpGet(builder.build());
    
    return execute(req,ContentResponse.class);
  }
  
  /**
   * Lists content.
   * @param groupId group id
   * @param num number items to return
   * @param start start item
   * @param token token (optional)
   * @return content response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public ContentResponse listGroupContent(String groupId, long num, long start, String token) throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(groupUri(groupId));
    
    builder.setParameter("f", "json");
    builder.setParameter("num", Long.toString(num));
    builder.setParameter("start", Long.toString(start));
    if (token!=null) {
      builder.setParameter("token", token);
    }
    HttpGet req = new HttpGet(builder.build());
    
    return execute(req,ContentResponse.class);
  }
  
  /**
   * Lists public content. Only specified item types will be included. See config.properties file.
   * @param num number items to return
   * @param start start item
   * @return content response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public QueryResponse listPublicContent(long num, long start) throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(searchUri());
    
    builder.setParameter("f", "json");
    builder.setParameter("num", Long.toString(num));
    builder.setParameter("start", Long.toString(start));
    
    String type = Arrays.stream(Config.readTypes()).map(s->StringUtils.trimToNull(s)).filter(s->s != null).collect(Collectors.joining(") OR type: ("));
    String q = String.format("type: (%s)", type);
    builder.setParameter("q", q);
    
    HttpGet req = new HttpGet(builder.build());
    
    return execute(req,QueryResponse.class);
  }
  
  /**
   * Lists folders.
   * @param owner owner
   * @param token token
   * @return array of folders
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public FolderEntry[] listFolders(String owner, String token) throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(userUri(owner, null));
    
    builder.setParameter("f", "json");
    if (token!=null) {
      builder.setParameter("token", token);
    }
    HttpGet req = new HttpGet(builder.build());
    
    return execute(req,ContentResponse.class).folders;
  }
  
  /**
   * Lists folders.
   * @param owner owner
   * @param token token
   * @return array of folders
   * @throws URISyntaxException if invalid URL
   * @throws IOException if operation fails
   */
  public Group[] listGroups(String owner, String token) throws URISyntaxException, IOException {
    URIBuilder builder = new URIBuilder(communityUserUri(owner));
    
    builder.setParameter("f", "json");
    if (token!=null) {
      builder.setParameter("token", token);
    }
    HttpGet req = new HttpGet(builder.build());
    
    return execute(req,User.class).groups;
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
   * @return token response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if accessing token fails
   */
  public TokenResponse generateToken(int minutes) throws URISyntaxException, IOException {
    HttpPost req = new HttpPost(generateTokenUri());
    
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    if (credentials != null) {
      params.put("username", StringUtils.trimToEmpty(credentials.getUserName()));
      params.put("password", StringUtils.trimToEmpty(credentials.getPassword()));
    }
    params.put("client", "requestip");
    params.put("expiration", Integer.toString(minutes));
    
    req.setEntity(createEntity(params));

    return execute(req,TokenResponse.class);
  }
  
  private Map<String, String> makeStdParams(String title, String description, ItemType itemType, URL thumbnailUrl, Double [] extent, String [] typeKeywords, String [] tags, String token) {
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    params.put("title", title);
    params.put("description", description);
    params.put("type", itemType.getTypeName());
    if (thumbnailUrl!=null) {
      params.put("thumbnailurl", thumbnailUrl.toExternalForm());
    }
    if (extent!=null && extent.length==4) {
      params.put("extent",Arrays.asList(extent).stream().map(Object::toString).collect(Collectors.joining(",")));
    }
    if (typeKeywords!=null) {
      params.put("typeKeywords", Arrays.asList(typeKeywords).stream().collect(Collectors.joining(",")));
    }
    if (tags!=null) {
      params.put("tags", Arrays.asList(tags).stream().collect(Collectors.joining(",")));
    }
    params.put("token", token);
    
    return params;
  }
  
  private URI itemInfoUri(String itemId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/content/items/" + itemId );
    return builder.build();
  }
  
  private URI itemMetaUri(String itemId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/content/items/" + itemId + "/info/metadata/metadata.xml");
    return builder.build();
  }
  
  private URI updateItemUri(String owner, String folderId, String itemId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + owner + (folderId!=null? "/" +folderId: "") +"/items/" + itemId + "/update");
    return builder.build();
  }
  
  private URI addItemUri(String owner, String folderId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + owner + (folderId!=null? "/" +folderId: "") +"/addItem");
    return builder.build();
  }
  
  private URI shareUri(String owner, String folderId, String itemId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + owner + (folderId!=null? "/" +folderId: "") +"/items/" + itemId + "/share");
    return builder.build();
  }
  
  private URI deleteUri(String owner, String folderId, String itemId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + owner + (folderId!=null? "/" +folderId: "") +"/items/" + itemId + "/delete");
    return builder.build();
  }
  
  private URI userUri(String owner, String folderId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/users/" + owner + (folderId!=null? "/"+folderId: ""));
    return builder.build();
  }
  
  private URI groupUri(String groupId) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/content/groups/" + groupId);
    return builder.build();
  }
  
  private URI communityUserUri(String owner) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(rootUrl.toURI().getScheme())
           .setHost(rootUrl.toURI().getHost())
           .setPort(rootUrl.toURI().getPort())
           .setPath(rootUrl.toURI().getPath() + "sharing/rest/community/users/" + owner );
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
    URI uri = rootUrl.toURI().resolve("sharing/rest/generateToken");
    return new URI("https", uri.getSchemeSpecificPart(), uri.getFragment());
  }
  
  private HttpEntity createEntity(Map<String, String> params) throws UnsupportedEncodingException {
    return  new UrlEncodedFormEntity(params.entrySet().stream()
            .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList()), "UTF-8");
  }
  
  private HttpEntity createEntity(Map<String, String> params, File file) throws UnsupportedEncodingException {
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.setMode(HttpMultipartMode.RFC6532);
    params.entrySet().stream().forEach(e->builder.addPart(e.getKey(), new StringBody(e.getValue(), ContentType.MULTIPART_FORM_DATA)));
    builder.addBinaryBody("file", file);
    return builder.build();
  }
  
  private <T> T execute(HttpUriRequest req, Class<T> clazz) throws IOException {
    String responseContent = execute(req, 0);
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper.readValue(responseContent, clazz);
  }
  
  private String execute(HttpUriRequest req, Integer redirectDepth) throws IOException {
    // Determine if we've reached the limit of redirection attempts
    if (redirectDepth > this.maxRedirects) {
      throw new HttpResponseException(HttpStatus.SC_GONE, "Too many redirects, aborting");
    }

    try (CloseableHttpResponse httpResponse = httpClient.execute(req); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      } else if (httpResponse.getStatusLine().getStatusCode() >= 300) {
        // See if we can redirect the command
        Header locationHeader = httpResponse.getFirstHeader("Location");
        if (locationHeader != null) {
          try {
            HttpRequestWrapper newReq = HttpRequestWrapper.wrap(req);

            // Determine if this is a relataive redirection
            URI redirUrl = new URI(locationHeader.getValue());
            if (!redirUrl.isAbsolute()) {
              HttpHost target = URIUtils.extractHost(newReq.getURI());

              redirUrl = URI.create(
                String.format(
                  "%s://%s%s",
                  target.getSchemeName(),
                  target.toHostString(),
                  locationHeader.getValue()
                )
              );
            }
            
            newReq.setURI(redirUrl);

            return execute(newReq, ++redirectDepth);
          } catch (IOException | URISyntaxException e) {
            LOG.debug("Error executing request", e);
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
          }
        }
      }
      return IOUtils.toString(contentStream, "UTF-8");
    }
  }

  private URL adjustUrl(URL rootUrl) {
    try {
      return new URL(rootUrl.toExternalForm().replaceAll("/*$", "/"));
    } catch (MalformedURLException ex) {
      return rootUrl;
    }
  }
  
  /**
   * Metadata format.
   */
  public static enum MetadataFormat {
    DEFAULT, ISO19115, FGDC, INSPIRE;
    
    @Override
    public String toString() {
      return name().toLowerCase();
    }
    
    public static MetadataFormat parse(String fmt, MetadataFormat def) {
      return Arrays.stream(MetadataFormat.values()).filter(f -> f.name().equalsIgnoreCase(fmt)).findFirst().orElse(def);
    }
  }
}
