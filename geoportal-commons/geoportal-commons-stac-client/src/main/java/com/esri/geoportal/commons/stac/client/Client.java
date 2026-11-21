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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STAC client.
 */
public class Client implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private final CloseableHttpClient httpClient;
    private URL url = null;
    private SimpleCredentials cred = null;
    private String collectionId = "";
    private TokenInfo tokenInfo;
    private final static ObjectMapper mapper = new ObjectMapper();

    private static final String TOKEN_URL = "oauth2/token";
    

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Creates instance of the client.
     *
     * @param httpClient HTTP client
     */
    public Client(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Creates instance of the client.
     *
     * @param httpClient HTTP client
     * @param url URL of the GPT REST end point
     * @param cred credentials
     * @param collectionId collection id
     */
    public Client(CloseableHttpClient httpClient, URL url, SimpleCredentials cred, String collectionId) {
        this.httpClient = httpClient;
        this.url = url;
        this.cred = cred;
        this.collectionId = collectionId;

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Creates instance of the client.
     *
     * @param url URL of the GPT REST end point
     * @param cred credentials
     * @param collectionId collections id
     */
    public Client(URL url, SimpleCredentials cred, String collectionId) {
        this(HttpClientBuilder.create().useSystemProperties().setRedirectStrategy(LaxRedirectStrategy.INSTANCE).build(),
                url,
                cred,
                collectionId);
    }

    @Override
    public void close() throws IOException {
        if (httpClient instanceof Closeable) {
            ((Closeable) httpClient).close();
        }
    }

    public ResponseWrapper<Item> readItem(URL itemUrl) throws IOException, URISyntaxException {
        HttpUriRequest GET = new HttpGet(itemUrl.toURI());
        ResponseWrapper<Item> itemWrapper = execute(GET, Item.class);
        return itemWrapper;
    }
    

    /**
     * Publishes a document.
     *
     * @param data data to publish
     * @param attributes extra attributes
     * @param json json
     * @return response information
     * @throws IOException if reading response fails
     * @throws URISyntaxException if URL has invalid syntax
     */
    public PublishResponse publishItem(
            PublishRequest data,
            Map<String, Object> attributes,
            String json) throws IOException, URISyntaxException {

        //ObjectNode jsonRequest = mapper.convertValue(data, ObjectNode.class);
        
        //String strRequest = mapper.writeValueAsString(jsonRequest);
        StringEntity entity = new StringEntity(json, "UTF-8");

        URI pubUri = createCollectionItemsUri("portal");
        return publish(pubUri, entity, data.sys_owner_s);
    }    
    
    private PublishResponse publish(URI uri, StringEntity entity, String owner) throws IOException, URISyntaxException {
        HttpPost post = new HttpPost(uri);
        post.setConfig(DEFAULT_REQUEST_CONFIG);
        post.setEntity(entity);
        post.setHeader("Content-Type", "application/json; charset=UTF-8");
        post.setHeader("User-Agent", HttpConstants.getUserAgent());
	    if (cred != null && !cred.isEmpty()) {
	    	  post.setHeader("Authorization","Bearer "+getAccessToken());
	    }
        ResponseWrapper<PublishResponse> response = execute(post, PublishResponse.class);

        return response != null ? response.data : null;
    }

    public Catalog readCatalog(URL catalogUrl) throws IOException, URISyntaxException {
        HttpUriRequest GET = new HttpGet(catalogUrl.toURI());
        ResponseWrapper<Catalog> wrapper = execute(GET, Catalog.class);
        return wrapper != null ? wrapper.data : null;
    }

    private <T> ResponseWrapper<T> makeResponseWrapper(String responseContent, URL url, Class<T> clazz) throws JsonProcessingException {
        T value = mapper.readValue(responseContent, clazz);
        return new ResponseWrapper<>(value, responseContent, url);
    }

    private <T> ResponseWrapper<T> execute(HttpUriRequest req, Class<T> clazz) throws IOException, URISyntaxException {
        try (CloseableHttpResponse httpResponse = httpClient.execute(req); InputStream contentStream = httpResponse.getEntity().getContent();) {
            String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
            int reasonCode = httpResponse.getStatusLine().getStatusCode();
            String responseContent = IOUtils.toString(contentStream, "UTF-8");
            LOG.trace(String.format("RESPONSE: %s, %s, %s", reasonCode, responseContent, reasonMessage));

            if (httpResponse.getStatusLine().getStatusCode() >= 400) {
                ResponseWrapper<T> wrapper = null;
                try {
                    wrapper = makeResponseWrapper(responseContent, req.getURI().toURL(), clazz);
                } catch (Exception ex) {
                    throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
                }
                if (wrapper == null) {
                    throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
                }
                return wrapper;
            }

            return makeResponseWrapper(responseContent, req.getURI().toURL(), clazz);
        }
    }
    
    private URI createCollectionItemsUri(String collectionId) throws URISyntaxException, IOException {
        URIBuilder b = new URIBuilder(url.toURI().resolve("collections/" + collectionId + "/items"));
        return b.build();
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
        String sOauth2Url = url.toString().replace("/stac","");
        URL oauth2Url = new URL(sOauth2Url);
        HttpPost post = new HttpPost(oauth2Url.toURI().resolve(TOKEN_URL));
        post.setConfig(DEFAULT_REQUEST_CONFIG);
        post.setHeader("User-Agent", HttpConstants.getUserAgent());
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("Accept", "application/json");
        HashMap<String, String> params = new HashMap<>();
        if (cred != null) {
        	 
             params.put("client_id", StringUtils.trimToEmpty(cred.getUserName()));
             params.put("client_secret", StringUtils.trimToEmpty(cred.getPassword()));
        }
        params.put("grant_type", "client_credentials");
        params.put("scope", "api.write");
        HttpEntity entity = new UrlEncodedFormEntity(params.entrySet().stream()
                .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList()));
        post.setEntity(entity);

        ResponseWrapper<Token> theToken = execute(post, Token.class);
        return theToken != null ? theToken.data : null;
    }

    private void clearToken() {
        tokenInfo = null;
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
