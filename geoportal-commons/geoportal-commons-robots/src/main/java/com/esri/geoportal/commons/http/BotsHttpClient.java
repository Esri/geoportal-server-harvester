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
package com.esri.geoportal.commons.http;

import com.esri.geoportal.commons.robots.Access;
import com.esri.geoportal.commons.robots.Bots;
import static com.esri.geoportal.commons.robots.BotsUtils.requestAccess;
import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bots http client.
 */
public class BotsHttpClient implements HttpClient, Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(BotsHttpClient.class);
  private final HttpClient client;
  private final Bots bots;

  public BotsHttpClient(HttpClient client, Bots bots) {
    this.client = client;
    this.bots = bots;
  }

  public BotsHttpClient(Bots bots) {
    this.client = HttpClientBuilder.create().build();
    this.bots = bots;
  }

  @Override
  public HttpParams getParams() {
    return client.getParams();
  }

  @Override
  public ClientConnectionManager getConnectionManager() {
    return client.getConnectionManager();
  }

  @Override
  public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
    HttpRequestWrapper wrap = HttpRequestWrapper.wrap(request);
    adviseRobotsTxt(wrap.getURI());
    wrap.setURI(applyPHP(wrap.getURI()));
    return client.execute(wrap);
  }

  @Override
  public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
    HttpRequestWrapper wrap = HttpRequestWrapper.wrap(request);
    adviseRobotsTxt(wrap.getURI());
    wrap.setURI(applyPHP(wrap.getURI()));
    return client.execute(wrap, context);
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
    HttpRequestWrapper wrap = HttpRequestWrapper.wrap(request, target);
    adviseRobotsTxt(wrap.getURI());
    wrap.setURI(applyPHP(wrap.getURI()));
    return client.execute(wrap);
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
    HttpRequestWrapper wrap = HttpRequestWrapper.wrap(request, target);
    adviseRobotsTxt(wrap.getURI());
    wrap.setURI(applyPHP(wrap.getURI()));
    return client.execute(wrap, context);
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
    HttpRequestWrapper wrap = HttpRequestWrapper.wrap(request);
    adviseRobotsTxt(wrap.getURI());
    wrap.setURI(applyPHP(wrap.getURI()));
    return client.execute(wrap, responseHandler);
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
    HttpRequestWrapper wrap = HttpRequestWrapper.wrap(request);
    adviseRobotsTxt(wrap.getURI());
    wrap.setURI(applyPHP(wrap.getURI()));
    return client.execute(wrap, responseHandler, context);
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
    HttpRequestWrapper wrap = HttpRequestWrapper.wrap(request,target);
    adviseRobotsTxt(wrap.getURI());
    wrap.setURI(applyPHP(wrap.getURI()));
    return client.execute(wrap, responseHandler);
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
    HttpRequestWrapper wrap = HttpRequestWrapper.wrap(request,target);
    adviseRobotsTxt(wrap.getURI());
    wrap.setURI(applyPHP(wrap.getURI()));
    return client.execute(wrap, responseHandler, context);
  }

  @Override
  public void close() throws IOException {
    if (client instanceof Closeable) {
      ((Closeable)client).close();
    }
  }
  
  private Long resolveThrottleDelay() {
    return bots!=null && bots.getCrawlDelay()!=null? 1000L*bots.getCrawlDelay(): null;
  }

  private String getRelativePath(URI u) throws MalformedURLException {
    return String.format("%s%s%s", u.getPath() != null ? u.getPath() : "/", u.getQuery() != null ? "?" + u.getQuery() : "", u.getFragment()!= null ? "#" + u.getFragment() : "");
  }
  
  private String getProtocolHostPort(URI u) throws MalformedURLException {
    return String.format("%s://%s%s", u.getScheme(), u.getHost(), u.getPort() >= 0 ? ":" + u.getPort() : "");
  }

  private void adviseRobotsTxt(URI u) throws IOException {
    if (bots != null) {
      String url = getRelativePath(u);
      LOG.debug(String.format("Evaluating access to %s using robots.txt", u));
      Access access = requestAccess(bots, url);
      if (!access.hasAccess()) {
        LOG.info(String.format("Access to %s disallowed by robots.txt", u));
        throw new HttpResponseException(403, String.format("Access to %s disallowed by robots.txt", url));
      }
      LOG.debug(String.format("Access to %s allowed by robots.txt", u));
      CrawlLocker.getInstance().enterServer(getProtocolHostPort(u), resolveThrottleDelay());
    }
  }
  
  private URI applyPHP(URI uri) throws ClientProtocolException  {
    if (bots!=null) {
      try {
        String orgUri = uri.toString();
        PHP php = parsePHP(bots.getHost());
        uri = updateURI(uri,php);
        if (!uri.equals(orgUri)) {
          LOG.debug(String.format("Uri updated from %s to %s", orgUri, uri));
        }
      } catch (URISyntaxException ex) {
        throw new ClientProtocolException("Unable to apply host robots.txt host directive.", ex);
      }
    }
    return uri;
  }
  
  private  URI updateURI(URI uri, PHP php) throws URISyntaxException  {
    return new URI(
            php.protocol!=null? php.protocol: uri.getScheme(), 
            uri.getUserInfo(), 
            php.host!=null? php.host: uri.getHost(), 
            php.host!=null? php.port!=null? php.port: -1: uri.getPort(), 
            uri.getPath(), 
            uri.getQuery(), 
            uri.getFragment()
    );
  }
  
  private PHP parsePHP(String host) {
    host = StringUtils.trimToEmpty(host);
    if (!host.isEmpty()) {
      // parse protocol
      String protocolPart = null;
      int protocolStopIdx = host.indexOf("://");
      if (protocolStopIdx>=0) {
        protocolPart = protocolStopIdx>0? host.substring(0,protocolStopIdx): null;
        host = host.substring(protocolStopIdx+"://".length());
      }
      
      // parse host:port
      String hostPart = null;
      Integer portPart = null;
      if (!host.isEmpty()) {
        int hostStopIdx = host.indexOf(":");
        if (hostStopIdx<0) {
          hostPart = host;
        } else {
          hostPart = hostStopIdx>0? host.substring(0, hostStopIdx): null;
          try {
            portPart = Integer.parseInt(host.substring(hostStopIdx+":".length()));
          } catch (NumberFormatException ex) {
            
          }
        }
      }
      
      if (protocolPart!=null || hostPart!=null || portPart!=null) {
        return new PHP(protocolPart, hostPart, portPart);
      }
    }
    return null;
  }
  
  /**
   * Protocol-host-port
   */
  private static class PHP {
    String  protocol;
    String  host;
    Integer port;

    public PHP(String protocol, String host, Integer port) {
      this.protocol = protocol;
      this.host = host;
      this.port = port;
    }
  }
}
