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
package com.esri.geoportal.commons.ags.client;

import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 * ArcGIS Server client.
 */
public class AgsClient implements Closeable {
  private static final URLCodec URL_CODEC = new URLCodec("UTF-8");

  private final URL rootUrl;
  private final CloseableHttpClient httpClient;
  private static final ObjectMapper mapper = new ObjectMapper();
  
  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /**
   * Creates instance of the client.
   *
   * @param httpClient HTTP client
   * @param rootUrl "arcgis/rest" root URL
   */
  public AgsClient(CloseableHttpClient httpClient, URL rootUrl) {
    this.rootUrl = adjustUrl(rootUrl);
    this.httpClient = httpClient;
  }

  @Override
  public void close() throws IOException {
    httpClient.close();
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
    HttpPost post = new HttpPost(rootUrl.toURI().resolve("tokens/generateToken"));
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    if (credentials != null) {
      params.put("username", StringUtils.trimToEmpty(credentials.getUserName()));
      params.put("password", StringUtils.trimToEmpty(credentials.getPassword()));
    }
    params.put("client", "requestip");
    params.put("expiration", Integer.toString(minutes));
    HttpEntity entity = new UrlEncodedFormEntity(params.entrySet().stream()
            .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList()));
    post.setEntity(entity);

    try (CloseableHttpResponse httpResponse = httpClient.execute(post); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      return mapper.readValue(responseContent, TokenResponse.class);
    }
  }

  /**
   * Lists folder content.
   *
   * @param folder folder or <code>null</code>
   * @return content response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if accessing token fails
   */
  public ContentResponse listContent(String folder) throws URISyntaxException, IOException {
    String url = rootUrl.toURI().resolve("rest/services/").resolve(StringUtils.stripToEmpty(folder)).toASCIIString();
    HttpGet get = new HttpGet(url + String.format("?f=%s", "json"));

    try (CloseableHttpResponse httpResponse = httpClient.execute(get); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      ContentResponse response = mapper.readValue(responseContent, ContentResponse.class);
      response.url = url;
      return response;
    }
  }

  /**
   * Reads service information.
   *
   * @param folder folder
   * @param si service info obtained through {@link listContent}
   * @return service response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if accessing token fails
   */
  public ServerResponse readServiceInformation(String folder, ServiceInfo si) throws URISyntaxException, IOException {
    String [] nameParts = si.name.split("/");
    for (int i=0; i<nameParts.length; i++) {
      nameParts[i] = URLEncoder.encode(nameParts[i], "UTF-8").replaceAll("\\+", "%20");
    }
    String name = StringUtils.join(nameParts, '/');
    String url = rootUrl.toURI()
      .resolve("rest/services/")
      .resolve(URLEncoder.encode(StringUtils.stripToEmpty(folder), "UTF-8").replaceAll("\\+", "%20"))
      .resolve(name + "/" + si.type)
      .toASCIIString();
    return readServiceInformation(new URL(url));
  }
  
  /**
   * Reads service information.
   *
   * @param url service URL
   * @return service response
   * @throws IOException if accessing token fails
   */
  public ServerResponse readServiceInformation(URL url) throws IOException {
    HttpGet get = new HttpGet(url + String.format("?f=%s", "json"));

    try (CloseableHttpResponse httpResponse = httpClient.execute(get); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      ServerResponse response = mapper.readValue(responseContent, ServerResponse.class);
      response.url = url.toExternalForm();
      response.json = responseContent;
      response.itemInfo = readItemInfo(new URL(url + "/info/itemInfo"));
      
      response.hasMetadata = false;
      response.metadataXML = "";
      String metadataURL = url + "/info/metadata";
      HttpGet getXML = new HttpGet(metadataURL);
      try (CloseableHttpResponse httpResponseXML = httpClient.execute(getXML); InputStream contentStreamXML = httpResponseXML.getEntity().getContent();) {
        if (httpResponseXML.getStatusLine().getStatusCode()<400) {
          String responseContentXML = IOUtils.toString(contentStreamXML, "UTF-8");
          response.metadataXML = responseContentXML;
          response.hasMetadata = true;
        }
      }
      
      return response;
    }
  }

  /**
   * Reads item information.
   * 
   * @param url item info url
   * @return item information
   * @throws IOException if accessing token fails
   */
  public ItemInfo readItemInfo(URL url) throws IOException {
    HttpGet get = new HttpGet(url + String.format("?f=%s", "json"));

    try (CloseableHttpResponse httpResponse = httpClient.execute(get); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.configure(Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      ItemInfo response = mapper.readValue(responseContent, ItemInfo.class);
                      
      return response;
    }
  }
  
  /**
   * Reads layer information.
   *
   * @param folder folder
   * @param si service info obtained through {@link listContent}
   * @param lRef layer reference
   * @return service response
   * @throws URISyntaxException if invalid URL
   * @throws IOException if accessing token fails
   */
  public LayerInfo readLayerInformation(String folder, ServiceInfo si, LayerRef lRef) throws URISyntaxException, IOException {
    String url = rootUrl.toURI().resolve("rest/services/").resolve(StringUtils.stripToEmpty(folder)).resolve(si.name + "/" + si.type + "/" + lRef.id).toASCIIString();
    HttpGet get = new HttpGet(url + String.format("?f=%s", "json"));

    try (CloseableHttpResponse httpResponse = httpClient.execute(get); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      LayerInfo response = mapper.readValue(responseContent, LayerInfo.class);
      if (response.hasMetadata) {
        HttpGet getXML = new HttpGet(url + String.format("/metadata", "text/xml"));
        try (CloseableHttpResponse httpResponseXML = httpClient.execute(getXML); InputStream contentStreamXML = httpResponseXML.getEntity().getContent();) {
          if (httpResponseXML.getStatusLine().getStatusCode()>=400) {
            throw new HttpResponseException(httpResponseXML.getStatusLine().getStatusCode(), httpResponseXML.getStatusLine().getReasonPhrase());
          }
          String responseContentXML = IOUtils.toString(contentStreamXML, "UTF-8");
          response.metadataXML = responseContentXML;
        }
          
      }
      response.url = url;
      response.json = responseContent;
      return response;
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
