/*
 * Copyright 2020 Esri, Inc.
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
package com.esri.geoportal.harvester.thredds;

import com.esri.geoportal.commons.constants.HttpConstants;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.constants.MimeTypeUtils;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.commons.thredds.client.Client;
import com.esri.geoportal.commons.thredds.client.Catalog;
import com.esri.geoportal.commons.thredds.client.Record;
import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 * THREDDS broker.
 */
/*package*/ class ThreddsBroker implements InputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(ThreddsBroker.class);

  private final ThreddsConnector connector;
  private final ThreddsBrokerDefinitionAdaptor definition;
  private final ArrayList<ThreddsIter> iterators = new ArrayList<>();

  protected CloseableHttpClient httpClient;
  private Client client;
  protected TaskDefinition td;

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition definition
   */
  public ThreddsBroker(ThreddsConnector connector, ThreddsBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    td = context.getTask().getTaskDefinition();
    CloseableHttpClient http = HttpClientBuilder.create().useSystemProperties().build();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      httpClient = http;
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), http, definition.getHostUrl());
      httpClient = new BotsHttpClient(http, bots);
    }
    client = new Client(httpClient, definition.getHostUrl());
  }

  @Override
  public void terminate() {
    new ArrayList<>(iterators).forEach(ThreddsIter::close);

    if (client!=null) {
      try {
        client.close();
      } catch (IOException ex) {
        LOG.error(String.format("Error terminating broker."), ex);
      }
    } else if (httpClient != null) {
      try {
        httpClient.close();
      } catch (IOException ex) {
        LOG.error(String.format("Error terminating broker."), ex);
      }
    }
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("THREDDS", definition.getHostUrl().toExternalForm(), null);
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    ThreddsIter iter = new ThreddsIter(iteratorContext) {
      @Override
      protected void onClose() {
        iterators.remove(this);
      }
    };

    iterators.add(iter);
    return iter;
  }

  private class ThreddsIter implements InputBroker.Iterator {
    private final IteratorContext iteratorContext;
    private LinkedList<URL> folders;
    private java.util.Iterator<Record> recordsIter;

    public ThreddsIter(IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      try {
        if (folders==null) {
          Catalog content = client.readCatalog(definition.getHostUrl());
          folders = new LinkedList<>(content.folders);
          recordsIter = content.records.iterator();
          
          return hasNext();
        }
        
        if (recordsIter==null || !recordsIter.hasNext()) {
          if (folders==null || folders.isEmpty()) return false;
          
          Catalog content = client.readCatalog(folders.pollFirst());
          folders.addAll(content.folders);
          recordsIter = content.records.iterator();
          
          return hasNext();
        }
          
        return recordsIter.hasNext();
      } catch (Exception ex) {
        throw new DataInputException(ThreddsBroker.this, String.format("Error retrieving content."), ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      if (recordsIter==null || !recordsIter.hasNext()) {
        throw new DataInputException(ThreddsBroker.this, String.format("No more records."));
      }
      Record rec = recordsIter.next();
      try {
        return readContent(rec.url, rec.id, iteratorContext.getLastHarvestDate());
      } catch (IOException|URISyntaxException ex) {
        throw new DataInputException(ThreddsBroker.this, String.format("Error reading content forL ", rec.id), ex);
      }
    }

    protected void onClose() {
      // called upon closing iterator
    }

    private void close() {
      onClose();
    }
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return true;
  }

  @Override
  public String toString() {
    return String.format("THREDDS [%s]", definition.getHostUrl());
  }

  @Override
  public InputConnector getConnector() {
    return connector;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  @Override
  public DataContent readContent(String id) throws DataInputException {
    try {
      return readContent(new URL(id), id, null);
    } catch (IOException|URISyntaxException ex) {
      throw new DataInputException(this, String.format("Error reading content for: %s", id), ex);
    }
  }

  /**
   * Reads content.
   * @param httpClient HTTP client
   * @param since since date
   * @return content reference
   * @throws IOException if reading content fails
   * @throws URISyntaxException if file url is an invalid URI
   */
  private SimpleDataReference readContent(URL url, String id, Date since) throws IOException, URISyntaxException {
    HttpGet method = new HttpGet(url.toURI());
    method.setConfig(DEFAULT_REQUEST_CONFIG);
    method.setHeader("User-Agent", HttpConstants.getUserAgent());
    
    try (CloseableHttpResponse httpResponse = httpClient.execute(method); InputStream input = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      if (Thread.currentThread().isInterrupted()) {
        return new SimpleDataReference(this.getBrokerUri(), this.getEntityDefinition().getLabel(), url.toExternalForm(), null, url.toURI(), this.td.getSource().getRef(), this.td.getRef());
      }
      Date lastModifiedDate = readLastModifiedDate(httpResponse);
      MimeType contentType = readContentType(httpResponse, url);
      boolean readBody = since==null || lastModifiedDate==null || lastModifiedDate.getTime()>=since.getTime();
      SimpleDataReference ref = new SimpleDataReference(this.getBrokerUri(), this.getEntityDefinition().getLabel(), id, lastModifiedDate, url.toURI(), this.td.getSource().getRef(), this.td.getRef());
      ref.addContext(contentType, readBody? IOUtils.toByteArray(input): null);

      // Adding in resource map attributes for saving to AGP...
      ref.getAttributesMap().put(WKAConstants.WKA_RESOURCE_URL, url.toURI());

      return ref;
    }
  }
  
  /**
   * Reads content type.
   * @param response HTTP response
   * @return content type or <code>null</code> if unable to read content type
   */
  private MimeType readContentType(HttpResponse response, URL url) {
    try {
      Header contentTypeHeader = response.getFirstHeader("Content-Type");
      MimeType contentType = null;
      if (contentTypeHeader!=null) {
        contentType = MimeType.parse(contentTypeHeader.getValue());
      }
      if (contentType==null) {
        String strFileUrl = url.toExternalForm();
        int lastDotIndex = strFileUrl.lastIndexOf(".");
        String ext = lastDotIndex>=0? strFileUrl.substring(lastDotIndex+1): "";
        contentType = MimeTypeUtils.mapExtension(ext);
      }
      return contentType;
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Reads last modified date.
   * @param response HTTP response
   * @return last modified date or <code>null</code> if unavailable
   */
  private Date readLastModifiedDate(HttpResponse response) {
    try {
      Header lastModifedHeader = response.getFirstHeader("Last-Modified");
      return lastModifedHeader != null
              ? Date.from(ZonedDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(lastModifedHeader.getValue())).toInstant())
              : null;
    } catch (Exception ex) {
      return null;
    }
  }
}
