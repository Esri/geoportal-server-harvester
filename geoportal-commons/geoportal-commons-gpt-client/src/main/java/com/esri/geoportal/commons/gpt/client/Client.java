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

import com.esri.core.geometry.MultiPoint;
import com.esri.geoportal.commons.constants.HttpConstants;
import com.esri.geoportal.commons.gpt.client.QueryResponse.Hit;
import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.geoportal.commons.geometry.GeometryService;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GPT 2.0 Client.
 */
public class Client implements Closeable {

  private final Logger LOG = LoggerFactory.getLogger(Client.class);

  private static final String DEFAULT_INDEX = "metadata";
  private static final String REST_ITEM_URL = "rest/metadata/item";
  private static final String ELASTIC_SEARCH_URL = "elastic/{metadata}/item/_search";
  private static final String ELASTIC_SCROLL_URL = "elastic/_search/scroll";
  private static final String TOKEN_URL = "oauth/token";

  private final CloseableHttpClient httpClient;
  private final GeometryService gs;
  private final URL url;
  private final SimpleCredentials cred;
  private final String index;

  private TokenInfo tokenInfo;

  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Creates instance of the client.
   *
   * @param httpClient HTTP client
   * @param gs geometry service
   * @param url URL of the GPT REST end point
   * @param cred credentials
   * @param index index name
   */
  public Client(CloseableHttpClient httpClient, GeometryService gs, URL url, SimpleCredentials cred, String index) {
    this.httpClient = httpClient;
    this.gs = gs;
    this.url = url;
    this.cred = cred;
    this.index = StringUtils.defaultIfBlank(index, DEFAULT_INDEX);

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /**
   * Creates instance of the client.
   *
   * @param gs geometry service
   * @param url URL of the GPT REST end point
   * @param cred credentials
   * @param index index name
   */
  public Client(GeometryService gs, URL url, SimpleCredentials cred, String index) {
    this(HttpClientBuilder.create().build(), gs, url, cred, index);
  }

  /**
   * Publishes a document.
   *
   * @param data data to publish
   * @param id custom id
   * @param xml xml
   * @param json json
   * @param forceAdd <code>true</code> to force add.
   * @return response information
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public PublishResponse publish(PublishRequest data, String id, String xml, String json, boolean forceAdd) throws IOException, URISyntaxException {

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

              MultiPoint mp = new MultiPoint();
              mp.add(xmin, ymin);
              mp.add(xmax, ymax);

              JsonNode spatialReference = fullExtent.get("spatialReference");
              if (spatialReference != null && spatialReference.has("wkid")) {
                int wkid = spatialReference.get("wkid").asInt();
                if (wkid != 4326) {
                  MultiPoint mp2 = gs.project(mp, wkid, 4326);
                  if (mp2.getPointCount()==2) {
                    mp = mp2;
                  }
                }
              }

              ObjectNode envelope_geo = mapper.createObjectNode();
              envelope_geo.put("type", "envelope");
              ArrayNode coordinates = mapper.createArrayNode();
              ArrayNode southWest = coordinates.addArray();
              southWest.add(mp.getPoint(0).getX());
              southWest.add(mp.getPoint(0).getY());
              ArrayNode northEast = coordinates.addArray();
              northEast.add(mp.getPoint(1).getX());
              northEast.add(mp.getPoint(1).getY());
              envelope_geo.set("coordinates", coordinates);
              
              jsonRequest.set("envelope_geo", envelope_geo);
              
              double lon = (mp.getPoint(0).getX() + mp.getPoint(1).getX()) / 2.0;
              double lat = (mp.getPoint(0).getY() + mp.getPoint(1).getY()) / 2.0;
              
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
                jsonRequest.set(String.format("%s_obj", fld.getKey()), fld.getValue());
                break;
            }
          }
        }
      } catch (Exception ex) {
        LOG.debug(String.format("Invalid json received.", json), ex);
      }
    }

    String strRequest = mapper.writeValueAsString(jsonRequest);
    StringEntity entity = new StringEntity(strRequest, "UTF-8");

    List<String> ids = !forceAdd ? queryIds(data.src_uri_s) : Collections.emptyList();

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

  private PublishResponse changeOwner(String id, String owner) throws IOException, URISyntaxException {
    URI uri = createChangeOwnerUri(id, owner);
    HttpPut put = new HttpPut(uri);
    put.setConfig(DEFAULT_REQUEST_CONFIG);
    put.setHeader("Content-Type", "application/json; charset=UTF-8");
    put.setHeader("User-Agent", HttpConstants.getUserAgent());

    return execute(put, PublishResponse.class);
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
      return readContent(jsonUri);
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

  private EntryRef readItem(URI uri) throws URISyntaxException, IOException {
    HttpGet get = new HttpGet(uri);
    get.setConfig(DEFAULT_REQUEST_CONFIG);
    get.setHeader("User-Agent", HttpConstants.getUserAgent());
    Hit hit = execute(get, Hit.class);
    return new EntryRef(hit._id, readUri(hit._source, uri), readLastUpdated(hit._source, new Date()));
  }

  /**
   * Returns listIds of ids.
   *
   * @return listIds of ids or <code>null</code> if no more ids.
   * @throws IOException if reading response fails
   * @throws URISyntaxException if URL has invalid syntax
   */
  public List<String> listIds() throws URISyntaxException, IOException {
    return queryIds(null, null, 200);
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

  private URI createChangeOwnerUri(String id, String owner) throws URISyntaxException, IOException {
    return new URIBuilder(url.toURI().resolve(REST_ITEM_URL + "/" + id + "/owner/" + owner))
            .addParameter("access_token", getAccessToken())
            .build();
  }

  private URI createXmlUri(String id) throws URISyntaxException, IOException {
    return new URIBuilder(url.toURI().resolve(REST_ITEM_URL + "/" + id + "/xml"))
            .addParameter("access_token", getAccessToken())
            .build();
  }

  private URI createJsonUri(String id) throws URISyntaxException, IOException {
    return new URIBuilder(url.toURI().resolve(REST_ITEM_URL + "/" + id + "/json"))
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
   *
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
      QueryResponse response = query(uri);
      searchContext._scroll_id = response._scroll_id;
      return response;
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode() == 401) {
        clearToken();
        uri = createQueryUri(term, value, size, searchContext);
        QueryResponse response = query(uri);
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

  private QueryResponse query(URI uri) throws IOException, URISyntaxException {
    HttpGet get = new HttpGet(uri);
    get.setConfig(DEFAULT_REQUEST_CONFIG);
    get.setHeader("Content-Type", "application/json");
    get.setHeader("User-Agent", HttpConstants.getUserAgent());

    return execute(get, QueryResponse.class);
  }

  private String createElasticSearchUrl() {
    return ELASTIC_SEARCH_URL.replaceAll("\\{metadata\\}", index);
  }

  private URI createQueryUri(String term, String value, long size, SearchContext searchContext) throws IOException, URISyntaxException {
    if (searchContext._scroll_id == null) {
      return new URIBuilder(url.toURI().resolve(createElasticSearchUrl()))
              .addParameter("q", term != null && value != null ? String.format("%s:\"%s\"", term, value) : "*:*")
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
      if (token.access_token==null) {
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

  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
    gs.close();
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
