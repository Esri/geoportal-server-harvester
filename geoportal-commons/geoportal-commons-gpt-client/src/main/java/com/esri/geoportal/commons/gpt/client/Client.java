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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GPT 2.0 Client.
 */
public class Client implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(Client.class);
  private static final int BATCH_SIZE = 500;

  private static final String DEFAULT_INDEX = "metadata";
  private static final String REST_ITEM_URL = "rest/metadata/item";
  private static final String ELASTIC_SEARCH_URL = "elastic/{metadata}/item/_search";
  private static final String ELASTIC_SCROLL_URL = "elastic/_search/scroll";
  private static final String TOKEN_URL = "oauth/token";

  private final CloseableHttpClient httpClient;
  private final URL url;
  private final SimpleCredentials cred;
  private final String index;
  private final String collectionsFieldName;

  private TokenInfo tokenInfo;

  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Creates instance of the client.
   *
   * @param httpClient HTTP client
   * @param url URL of the GPT REST end point
   * @param cred credentials
   * @param index index name
   * @param collectionsFieldName collections field name
   */
  public Client(CloseableHttpClient httpClient, URL url, SimpleCredentials cred, String index, String collectionsFieldName) {
    this.httpClient = httpClient;
    this.url = url;
    this.cred = cred;
    this.index = StringUtils.defaultIfBlank(index, DEFAULT_INDEX);
    this.collectionsFieldName = collectionsFieldName;

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /**
   * Creates instance of the client.
   *
   * @param url URL of the GPT REST end point
   * @param cred credentials
   * @param index index name
   * @param collectionsFieldName collections field name
   */
  public Client(URL url, SimpleCredentials cred, String index, String collectionsFieldName) {
    this(HttpClientBuilder.create().useSystemProperties().build(), url, cred, index, collectionsFieldName);
  }

  /**
   * Publishes a document.
   *
   * @param data data to publish
   * @param attributes extra attributes
   * @param id custom id
   * @param xml xml
   * @param json json
   * @param forceAdd <code>true</code> to force add.
   * @param collections list of collections
   * @return response information
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public PublishResponse publish(
    PublishRequest data, 
    Map<String, Object> attributes, 
    String id, 
    String xml, String json, 
    boolean forceAdd,
    String [] collections) throws IOException, URISyntaxException {

    ObjectNode jsonRequest = mapper.convertValue(data, ObjectNode.class);
    if (xml != null) {
      jsonRequest.put("xml", xml);
    }
    if (json != null) {
      try {
        ObjectNode jsonValue = mapper.readValue(json, ObjectNode.class);
        jsonRequest.set("_json", jsonValue);
        if (jsonValue.isObject()) {
          Iterator<Map.Entry<String, JsonNode>> fldIter = jsonValue.fields();
          while (fldIter.hasNext()) {
            Map.Entry<String, JsonNode> fld = fldIter.next();

            if (fld.getKey().equals("fullExtent")) {
              JsonNode fullExtent = fld.getValue();

              Double xmin = fullExtent.path("xmin").asDouble();
              Double ymin = fullExtent.path("ymin").asDouble();
              Double xmax = fullExtent.path("xmax").asDouble();
              Double ymax = fullExtent.path("ymax").asDouble();

              ObjectNode envelope_geo = mapper.createObjectNode();
              envelope_geo.put("type", "envelope");
              ArrayNode coordinates = mapper.createArrayNode();
              ArrayNode southWest = coordinates.addArray();
              southWest.add(Math.max(xmin, -180.0));
              southWest.add(Math.min(ymax, 90.0));
              ArrayNode northEast = coordinates.addArray();
              northEast.add(Math.min(xmax, 180.0));
              northEast.add(Math.max(ymin, -90.0));
              envelope_geo.set("coordinates", coordinates);

              jsonRequest.set("envelope_geo", envelope_geo);

              double lon = (xmin + xmax) / 2.0;
              double lat = (ymin + ymax) / 2.0;

              ObjectNode envelope_cen_pt = mapper.createObjectNode();
              envelope_cen_pt.put("lon", lon);
              envelope_cen_pt.put("lat", lat);

              jsonRequest.set("envelope_cen_pt", envelope_cen_pt);
            }

            switch (fld.getValue().getNodeType()) {
              case STRING:
                String s_format = "%s_txt";
                switch (fld.getKey()) {
                  case "allowedUploadFileTypes":
                  case "capabilities":
                  case "configuredState":
                  case "clusterName":
                  case "executionType":
                  case "geometryType":
                  case "htmlPopupType":
                  case "isolationLevel":
                  case "loadBalancing":
                  case "supportedQueryFormats":
                  case "tags":
                  case "type":
                  case "typeName":
                  case "units":
                    s_format = "%s_s";
                    break;
                  case "title":
                  case "description":
                  case "fileid":
                    s_format = "%s";
                    break;
                }
                jsonRequest.put(String.format(s_format, fld.getKey()), fld.getValue().asText());
                break;
              case NUMBER:
                jsonRequest.put(String.format("%s_d", fld.getKey()), fld.getValue().asDouble());
                break;
              case BOOLEAN:
                jsonRequest.put(String.format("%s_b", fld.getKey()), fld.getValue().asBoolean());
                break;
              case ARRAY:
                jsonRequest.set(String.format("%s", fld.getKey()), fld.getValue());
                break;
              case OBJECT:
//                jsonRequest.set(String.format("%s_obj", fld.getKey()), fld.getValue());
                break;
            }
          }
        }
      } catch (Exception ex) {
        LOG.debug(String.format("Invalid json received.", json), ex);
      }
    }

    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      if (entry.getValue() == null) {
        jsonRequest.putNull(entry.getKey());
      } else {
        if (entry.getValue() instanceof String) {
          jsonRequest.put(entry.getKey(), (String) entry.getValue());
        } else if (entry.getValue() instanceof Double) {
          jsonRequest.put(entry.getKey(), (Double) entry.getValue());
        } else if (entry.getValue() instanceof BigDecimal) {
          jsonRequest.put(entry.getKey(), ((BigDecimal) entry.getValue()).doubleValue());
        } else if (entry.getValue() instanceof Float) {
          jsonRequest.put(entry.getKey(), (Float) entry.getValue());
        } else if (entry.getValue() instanceof Long) {
          jsonRequest.put(entry.getKey(), (Long) entry.getValue());
        } else if (entry.getValue() instanceof BigInteger) {
          jsonRequest.put(entry.getKey(), ((BigInteger) entry.getValue()).longValue());
        } else if (entry.getValue() instanceof Integer) {
          jsonRequest.put(entry.getKey(), (Integer) entry.getValue());
        } else if (entry.getValue() instanceof Boolean) {
          jsonRequest.put(entry.getKey(), (Boolean) entry.getValue());
        } else if (entry.getValue() instanceof JsonNode) {
          jsonRequest.set(entry.getKey(), (JsonNode) entry.getValue());
        }
      }
    }

    if (collections!=null) {
      List<String> collectionsList = Arrays.stream(collections)
        .map(StringUtils::trimToNull)
        .filter(collection -> collection!=null)
        .collect(Collectors.toList());
      
      if (!collectionsList.isEmpty()) {
        ArrayNode collectionsArray = jsonRequest.putArray(collectionsFieldName);
        collectionsList.forEach(collectionsArray::add);
      }
    }
    
    String strRequest = mapper.writeValueAsString(jsonRequest);
    StringEntity entity = new StringEntity(strRequest, "UTF-8");

    List<String> ids = !forceAdd ? queryIds("src_uri_s", data.src_uri_s, 1) : Collections.emptyList();

    URI pubUri = id != null ? createItemUri(id) : !ids.isEmpty() ? createItemUri(ids.get(0)) : createItemsUri();
    try {
      return publish(pubUri, entity, data.sys_owner_s);
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode() == 401) {
        clearToken();
        pubUri = id != null ? createItemUri(id) : !ids.isEmpty() ? createItemUri(ids.get(0)) : createItemsUri();
        return publish(pubUri, entity, data.sys_owner_s);
      } else {
        throw ex;
      }
    }
  }

  /**
   * Reads metadata.
   *
   * @param id id of the metadata
   * @return string representing metadata
   * @throws URISyntaxException if invalid URI
   * @throws IOException if reading metadata fails
   */
  public String readXml(String id) throws URISyntaxException, IOException {
    URI xmlUri = createXmlUri(id);
    try {
      return readContent(xmlUri);
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode() == 401) {
        clearToken();
        xmlUri = createXmlUri(id);
        return readContent(xmlUri);
      } else {
        throw ex;
      }
    }
  }

  /**
   * Reads metadata.
   *
   * @param id id of the metadata
   * @return string representing metadata
   * @throws URISyntaxException if invalid URI
   * @throws IOException if reading metadata fails
   */
  public String readJson(String id) throws URISyntaxException, IOException {
    URI jsonUri = createJsonUri(id);
    try {
      String content = readContent(jsonUri);
      try {
        JsonNode root = mapper.readTree(content);
        if (root.isObject() && root.has("_source") && root.get("_source").isObject() && root.get("_source").has("_json")) {
          JsonNode json = root.get("_source").get("_json");
          return mapper.writeValueAsString(json);
        }
      } catch (IOException ex) {
        // ignore
      }
      return null;
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode() == 401) {
        clearToken();
        jsonUri = createJsonUri(id);
        return readContent(jsonUri);
      } else {
        throw ex;
      }
    }
  }

  /**
   * Reads metadata.
   *
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
      if (ex.getStatusCode() == 401) {
        clearToken();
        itemUri = createItemUri(id);
        return readItem(itemUri);
      } else {
        throw ex;
      }
    }
  }

  /**
   * Returns listIds of ids.
   *
   * @return listIds of ids or <code>null</code> if no more ids.
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public List<String> listIds() throws URISyntaxException, IOException {
    return queryIds(null, null, BATCH_SIZE);
  }

  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
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
    return queryIds("src_source_uri_s", src_source_uri_s, BATCH_SIZE);
  }

  /**
   * Deletes record by id.
   *
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
      if (ex.getStatusCode() == 401) {
        clearToken();
        deleteUri = createItemUri(id);
        return delete(deleteUri);
      } else {
        throw ex;
      }
    }
  }

  private PublishResponse publish(URI uri, StringEntity entity, String owner) throws IOException, URISyntaxException {
    HttpPut put = new HttpPut(uri);
    put.setConfig(DEFAULT_REQUEST_CONFIG);
    put.setEntity(entity);
    put.setHeader("Content-Type", "application/json; charset=UTF-8");
    put.setHeader("User-Agent", HttpConstants.getUserAgent());

    PublishResponse response = execute(put, PublishResponse.class);
    if (response.getError() == null && owner != null) {
      changeOwner(response.getId(), owner);
    }

    return response;
  }

  private EntryRef readItem(URI uri) throws URISyntaxException, IOException {
    HttpGet get = new HttpGet(uri);
    get.setConfig(DEFAULT_REQUEST_CONFIG);
    get.setHeader("User-Agent", HttpConstants.getUserAgent());
    Hit hit = execute(get, Hit.class);
    return new EntryRef(hit._id, readUri(hit._source, uri), readLastUpdated(hit._source, new Date()));
  }

  private String readContent(URI uri) throws URISyntaxException, IOException {
    HttpGet get = new HttpGet(uri);
    get.setConfig(DEFAULT_REQUEST_CONFIG);
    get.setHeader("User-Agent", HttpConstants.getUserAgent());

    try (CloseableHttpResponse httpResponse = httpClient.execute(get); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode() >= 400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      return responseContent;
    }
  }

  private PublishResponse changeOwner(String id, String owner) throws IOException, URISyntaxException {
    URI uri = createChangeOwnerUri(id, owner);
    HttpPut put = new HttpPut(uri);
    put.setConfig(DEFAULT_REQUEST_CONFIG);
    put.setHeader("Content-Type", "application/json; charset=UTF-8");
    put.setHeader("User-Agent", HttpConstants.getUserAgent());

    return execute(put, PublishResponse.class);
  }

  private URI readUri(QueryResponse.Source source, URI defUri) {
    if (source != null && source.src_uri_s != null) {
      try {
        return new URI(source.src_uri_s);
      } catch (Exception ex) {
      }
    }
    return defUri;
  }

  private Date readLastUpdated(QueryResponse.Source source, Date defDate) {
    if (source != null && source.src_lastupdate_dt != null) {
      try {
        return Date.from(ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(source.src_lastupdate_dt)).toInstant());
      } catch (Exception ex) {
      }
    }
    return defDate;
  }

  private PublishResponse delete(URI uri) throws URISyntaxException, IOException {
    HttpDelete del = new HttpDelete(uri);
    del.setConfig(DEFAULT_REQUEST_CONFIG);
    del.setHeader("User-Agent", HttpConstants.getUserAgent());
    return execute(del, PublishResponse.class);
  }

  private URI createItemsUri() throws URISyntaxException, IOException {
    URIBuilder b = new URIBuilder(url.toURI().resolve(REST_ITEM_URL));
    if (cred != null && !cred.isEmpty()) {
      b.addParameter("access_token", getAccessToken());
    }
    return b.build();
  }

  private URI createItemUri(String id) throws URISyntaxException, IOException {
    URIBuilder b = new URIBuilder(url.toURI().resolve(REST_ITEM_URL + "/" + id));
    if (cred != null && !cred.isEmpty()) {
      b.addParameter("access_token", getAccessToken());
    }
    return b.build();
  }

  private URI createChangeOwnerUri(String id, String owner) throws URISyntaxException, IOException {
    return new URIBuilder(url.toURI().resolve(REST_ITEM_URL + "/" + id + "/owner/" + owner))
            .addParameter("access_token", getAccessToken())
            .build();
  }

  private URI createXmlUri(String id) throws URISyntaxException, IOException {
    URIBuilder b = new URIBuilder(url.toURI().resolve(REST_ITEM_URL + "/" + id + "/xml"));
    if (cred != null && !cred.isEmpty()) {
      b.addParameter("access_token", getAccessToken());
    }
    return b.build();
  }

  private URI createJsonUri(String id) throws URISyntaxException, IOException {
    URIBuilder b = new URIBuilder(url.toURI().resolve(REST_ITEM_URL + "/" + id));
    if (cred != null && !cred.isEmpty()) {
      b.addParameter("access_token", getAccessToken());
    }
    return b.build();
  }

  /**
   * Query ids.
   *
   * @param term term to query
   * @param value value of the term
   * @param batchSize batch size (note: size 1 indicates looking for the first
   * only)
   * @return listIds of ids
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  private List<String> queryIds(String term, String value, long batchSize) throws IOException, URISyntaxException {
    Set<String> ids = new HashSet<>();
    String search_after = null;

    ObjectNode root = mapper.createObjectNode();
    root.put("size", batchSize);
    root.set("_source", mapper.createArrayNode().add("_id"));
    root.set("sort", mapper.createArrayNode().add(mapper.createObjectNode().put("_id", "asc")));
    if (term != null && value != null) {
      root.set("query", mapper.createObjectNode().set("match", mapper.createObjectNode().put(term, value)));
    }

    do {
      URIBuilder builder = new URIBuilder(url.toURI().resolve(createElasticSearchUrl()));
      if (cred != null && !cred.isEmpty()) {
        builder = builder.addParameter("access_token", getAccessToken());
      }
    
      if (search_after != null) {
        root.set("search_after", mapper.createArrayNode().add(search_after));
      }

      String json = mapper.writeValueAsString(root);
      HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

      QueryResponse response = query(builder, entity);
      if (response!=null && response.status!=null && response.status==400 && search_after==null) {
        // This indicates it could be an old version of Elastic Search behind the Geoportal.
        // Fall back to using scroll API
        return queryIdsScroll(term, value, batchSize);
      }

      search_after = null;
      if (response != null && response.hasHits()) {
        List<String> responseIds = response.hits.hits.stream().map(hit -> hit._id).collect(Collectors.toList());
        ids.addAll(responseIds);

        // if argument 'size' is 1 that means looking for the first one only; otherwise looking for every possible
        search_after = batchSize > 1 ? responseIds.get(responseIds.size() - 1) : null;
      }
    } while (search_after != null && !Thread.currentThread().isInterrupted());

    return ids.stream().collect(Collectors.toList());
  }

  private List<String> queryIdsScroll(String term, String value, long size) throws IOException, URISyntaxException {
    ArrayList<String> ids = new ArrayList<>();
    SearchContext searchContext = new SearchContext();

    while (!Thread.currentThread().isInterrupted()) {
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

  private void clearToken() {
    tokenInfo = null;
  }

  private QueryResponse query(String term, String value, long size, SearchContext searchContext) throws IOException, URISyntaxException {
    URI uri = createQueryUri(searchContext);
    HttpEntity httpEntity = createQueryEntity(term, value, size, searchContext);
    try {
      QueryResponse response = query(uri, httpEntity);
      searchContext._scroll_id = response._scroll_id;
      return response;
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode() == 401) {
        clearToken();
        uri = createQueryUri(searchContext);
        httpEntity = createQueryEntity(term, value, size, searchContext);
        QueryResponse response = query(uri, httpEntity);
        searchContext._scroll_id = response._scroll_id;
        return response;
      } else {
        throw ex;
      }
    }
  }

  private QueryResponse query(URIBuilder builder, HttpEntity entity) throws IOException, URISyntaxException {
    if (cred != null && !cred.isEmpty()) {
      builder = builder.addParameter("access_token", getAccessToken());
    }

    QueryResponse response = null;
    try {
      response = query(builder.build(), entity);
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode() == 401) {
        clearToken();
        if (cred != null && !cred.isEmpty()) {
          builder = builder.addParameter("access_token", getAccessToken());
        }
        response = query(builder.build(), entity);
      } else {
        throw ex;
      }
    }

    return response;
  }

  private QueryResponse query(URI uri, HttpEntity httpEntity) throws IOException, URISyntaxException {
    HttpPost request = new HttpPost(uri);
    request.setEntity(httpEntity);

    request.setConfig(DEFAULT_REQUEST_CONFIG);
    request.setHeader("Content-Type", "application/json");
    request.setHeader("User-Agent", HttpConstants.getUserAgent());

    return execute(request, QueryResponse.class);
  }

  private String createElasticSearchUrl() {
    return ELASTIC_SEARCH_URL.replaceAll("\\{metadata\\}", index);
  }

  private HttpEntity createQueryEntity(String term, String value, long size, SearchContext searchContext) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    if (searchContext._scroll_id == null) {
      node.put("size", size);
      if (term != null && value != null) {
        ObjectNode query = mapper.createObjectNode();
        node.set("query", query);

        ObjectNode match = mapper.createObjectNode();
        query.set("match", match);

        match.put(term, value);
      }
    } else {
      node.put("scroll", "1m");
      node.put("scroll_id", searchContext._scroll_id);
    }

    return new StringEntity(node.toString(), ContentType.APPLICATION_JSON);
  }

  private URI createQueryUri(SearchContext searchContext) throws IOException, URISyntaxException {
    URIBuilder builder;

    if (searchContext._scroll_id == null) {
      builder = new URIBuilder(url.toURI().resolve(createElasticSearchUrl()))
              .addParameter("scroll", "1m");
    } else {
      builder = new URIBuilder(url.toURI().resolve(ELASTIC_SCROLL_URL))
              .addParameter("scroll_id", searchContext._scroll_id)
              .addParameter("scroll", "1m");
    }

    if (cred != null && !cred.isEmpty()) {
      builder = builder.addParameter("access_token", getAccessToken());
    }

    return builder.build();
  }

  private <T> T execute(HttpUriRequest req, Class<T> clazz) throws IOException, URISyntaxException {
    try (CloseableHttpResponse httpResponse = httpClient.execute(req); InputStream contentStream = httpResponse.getEntity().getContent();) {
      String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));

      if (httpResponse.getStatusLine().getStatusCode() >= 400) {
        T value = null;
        try {
          value = mapper.readValue(responseContent, clazz);
        } catch (Exception ex) {
          throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }
        if (value == null) {
          throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }
        return value;
      }

      return mapper.readValue(responseContent, clazz);
    }
  }

  private String getAccessToken() throws URISyntaxException, IOException {
    LocalDateTime now = LocalDateTime.now();
    if (tokenInfo == null || tokenInfo.validTill.minusSeconds(60).isBefore(now)) {
      Token token = generateToken();
      if (token.access_token == null) {
        throw new IOException("Error obtaining access token");
      }
      TokenInfo ti = new TokenInfo();
      ti.token = token;
      ti.validTill = now.plusSeconds(token.expires_in);
      tokenInfo = ti;
    }
    return tokenInfo.token.access_token;
  }

  private Token generateToken() throws URISyntaxException, UnsupportedEncodingException, IOException {
    HttpPost post = new HttpPost(url.toURI().resolve(TOKEN_URL));
    post.setConfig(DEFAULT_REQUEST_CONFIG);
    post.setHeader("User-Agent", HttpConstants.getUserAgent());
    post.setHeader("Content-Type", "application/x-www-form-urlencoded");
    post.setHeader("Accept", "application/json");
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

    return execute(post, Token.class);
  }

  /**
   * Search context.
   */
  public static class SearchContext {

    public String _scroll_id;
  }

  /**
   * Access token.
   */
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
