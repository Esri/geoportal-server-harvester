/*
 * Copyright 2021 Esri, Inc.
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
package com.esri.geoportal.commons.stac.client;

import com.esri.geoportal.commons.constants.HttpConstants;
import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STAC client.
 */
public class STACClient implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(STACClient.class);
  private final CloseableHttpClient httpClient;
  private final static ObjectMapper mapper = new ObjectMapper(); 
  private final SimpleCredentials credentials;
  private final URL rootUrl;

  
  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /**
   * Creates instance of the client.
   *
   * @param httpClient HTTP client
   */
  public STACClient(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
    this.credentials = null;
    this.rootUrl = null;
  } 

  public STACClient(CloseableHttpClient httpClient, URL rootUrl, SimpleCredentials credentials) {
    this.credentials = credentials;
    this.httpClient = httpClient;
    this.rootUrl = rootUrl;
  }
  
  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
  }
  
  public ResponseWrapper<Item> readItem(URL itemUrl)  throws IOException, URISyntaxException {
    HttpUriRequest GET = new HttpGet(itemUrl.toURI());
    ResponseWrapper<Item> itemWrapper = execute(GET, Item.class);
    return itemWrapper;
  }
  
  public Catalog readCatalog(URL catalogUrl)  throws IOException, URISyntaxException {
    HttpUriRequest GET = new HttpGet(catalogUrl.toURI());
    ResponseWrapper<Catalog> wrapper = execute(GET, Catalog.class);
    return wrapper!=null? wrapper.data: null;
  }

  public Collection getCollections(URL collectionUrl)  throws IOException, URISyntaxException {
    HttpUriRequest GET = new HttpGet(collectionUrl.toURI());
    ResponseWrapper<Collection> wrapper = execute(GET, Collection.class);
    return wrapper!=null? wrapper.data: null;
  }
  
  private <T> ResponseWrapper<T> makeResponseWrapper(String responseContent, URL url, Class<T> clazz) throws JsonProcessingException  {
      T value = mapper.readValue(responseContent, clazz);
      return new ResponseWrapper<>(value, responseContent, url);
  }
  
  private URL adjustUrl(URL rootUrl) {
    try {
      return new URL(rootUrl.toExternalForm().replaceAll("/*$", "/"));
    } catch (MalformedURLException ex) {
      return rootUrl;
    }
  }
    
  public String getItem(String itemId, String collectionId) {
    return "done";
  }
    
  /*
    Add the STAC item to the catalog
  */
  public PublishResponse publish(
          JSONObject stacItem, 
          String collectionId) {
    PublishResponse publishResponse = new PublishResponse();
    String itemId = (String) stacItem.get("id");

    String strRequest;
    try {
      //strRequest = mapper.writeValueAsString(stacItem.toString());
      StringEntity entity = new StringEntity(stacItem.toString(), "UTF-8");
      
      System.out.println("toJSONString --> " + stacItem.toJSONString());
      System.out.println("toString --> " + stacItem.toString());

      //this.credentials;
      String pubUrl = createItemUrl(collectionId);
      
      // 1. Create a CredentialsProvider and set credentials
      String up = credentials.toString();
      String encodedUP = java.util.Base64.getEncoder().encodeToString(up.getBytes());

      // 2. Build the HttpClient with the CredentialsProvider
      try (CloseableHttpClient stacAPIClient = HttpClientBuilder.create()
              //.setDefaultCredentialsProvider(credsProvider)
              .build()) {

          // 3. Create the HttpPost request
          HttpPost httpPost = new HttpPost(pubUrl);
          
          httpPost.setHeader("Content-Type", "application/json");
          httpPost.setHeader("Authorization", "Basic " + encodedUP);

          // Set any required entity (e.g., JSON payload)
          httpPost.setEntity(entity);
          

          // 4. Execute the request
          try (CloseableHttpResponse response = stacAPIClient.execute(httpPost)) {
              // Handle the response
              System.out.println("Response Status Code: " + response.getStatusLine().getStatusCode());
              System.out.println("Response Body: " + EntityUtils.toString(response.getEntity()));
          }
      }      
      
      publishResponse.setId(itemId);
      publishResponse.setStatus("done");     

    } catch (URISyntaxException | IOException ex) {
      java.util.logging.Logger.getLogger(STACClient.class.getName()).log(Level.SEVERE, null, ex);
      publishResponse.setStatus("ERROR");     

    }
    
    return publishResponse;
  }
  
  public String deleteItem(String itemId, String collectionId, String token) {
    return "done";
  }  
    
  public JSONObject search(String itemId) {
    return null;
  }

  private <T> ResponseWrapper<T> execute(HttpUriRequest req, Class<T> clazz) throws IOException, URISyntaxException {
    try (CloseableHttpResponse httpResponse = httpClient.execute(req); InputStream contentStream = httpResponse.getEntity().getContent();) {
      String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));
      
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        ResponseWrapper<T> wrapper = null;
        try {
          wrapper = makeResponseWrapper(responseContent, req.getURI().toURL(), clazz);
        } catch (JsonProcessingException | MalformedURLException ex) {
          throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }
        if (wrapper==null) {
          throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }
        return wrapper;
      }
      
      return makeResponseWrapper(responseContent, req.getURI().toURL(), clazz);
    }
  }

    private String createItemUrl(String collectionId) throws URISyntaxException, IOException {
        String b = this.rootUrl + "/collections/" + collectionId + "/items";
        return b;
    }
    
}
