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
package com.esri.geoportal.harvester.oai.pmh;

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.commons.oai.client.Client;
import com.esri.geoportal.commons.oai.client.Header;
import com.esri.geoportal.commons.oai.client.ListIdsResponse;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esri.geoportal.harvester.api.Initializable;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * OAI broker.
 */
/*package*/ class OaiBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(OaiBroker.class);
  
  private final OaiConnector connector;
  private final OaiBrokerDefinitionAdaptor definition;
  
  protected CloseableHttpClient httpClient;
  private Client client;
  private TaskDefinition td;
  
  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   */
  public OaiBroker(OaiConnector connector, OaiBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public void initialize(Initializable.InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    td = context.getTask().getTaskDefinition();
    CloseableHttpClient http = HttpClientBuilder.create().useSystemProperties().build();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      httpClient = http;
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), http, definition.getHostUrl());
      httpClient = new BotsHttpClient(http,bots);
    }
    client = new Client(httpClient, definition.getHostUrl(), definition.getPrefix(), definition.getSet());
  }

  @Override
  public void terminate() {
    if (httpClient!=null) {
      try {
        httpClient.close();
      } catch (IOException ex) {
        LOG.error(String.format("Error terminating broker."), ex);
      }
    }
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("OAI",definition.getHostUrl().toExternalForm(),null);
  }

  @Override
  public InputBroker.Iterator iterator(InputBroker.IteratorContext iteratorContext) throws DataInputException {
    return new OaiIterator(iteratorContext);
  }

  @Override
  public String toString() {
    return String.format("OAI [%s]", definition.getHostUrl());
  }

  @Override
  public InputConnector getConnector() {
    return connector;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }
    
  private String firstNonBlank(String...strs) {
    return Arrays.asList(strs).stream().filter(s->!StringUtils.isBlank(s)).findFirst().orElse(null);
  }
  
  /**
   * OAI-PMH iterator.
   */
  private class OaiIterator implements InputBroker.Iterator {
    private final InputBroker.IteratorContext iteratorContext;
    
    private java.util.Iterator<Header> idIter;
    private String resumptionToken;

    public OaiIterator(InputBroker.IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      try {
        if (idIter!=null && idIter.hasNext()) {
          return true;
        }

        ListIdsResponse listIds = client.listIds(resumptionToken, iteratorContext.getLastHarvestDate());
        if (listIds.headers.length>0) {
          idIter = Arrays.asList(listIds.headers).iterator();
          resumptionToken = listIds.resumptionToken;
          return true;
        } else if (listIds.resumptionToken!=null) {
          return hasNext();
        }
        
        return false;
      } catch (IOException|URISyntaxException|ParserConfigurationException|SAXException|XPathExpressionException ex) {
        throw new DataInputException(OaiBroker.this, String.format("Error reading data from: %s", this), ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      try {
        if (idIter==null || !idIter.hasNext()) {
          throw new DataInputException(OaiBroker.this, String.format("No more data available"));
        }
        
        Header header = idIter.next();
        String record = client.readRecord(header.identifier);

        SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), header.identifier, parseIsoDate(header.datestamp), URI.create(header.identifier), td.getSource().getRef(), td.getRef());
        ref.addContext(MimeType.APPLICATION_XML, record.getBytes("UTF-8"));
        
        return ref;
        
      } catch (URISyntaxException|IOException|ParserConfigurationException|SAXException|TransformerException|XPathExpressionException ex) {
        throw new DataInputException(OaiBroker.this, String.format("Error reading data from: %s", this), ex);
      }
    }
    
  }

  /**
   * Parses ISO date
   *
   * @param strDate ISO date as string
   * @return date object or <code>null</code> if unable to parse date
   */
  private Date parseIsoDate(String strDate) {
    if (strDate==null) {
      return null;
    }
    try {
      return Date.from(ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(strDate)).toInstant());
    } catch (Exception ex) {
      return null;
    }
  }
  
  /**
   * Content provided by the broker.
   */
  protected static final class Content {
    private final String data;
    private final MimeType contentType;
    
    /**
     * Creates instance of the content.
     * @param data content data
     * @param contentType content type
     */
    public Content(String data, MimeType contentType) {
      this.data = data;
      this.contentType = contentType;
    }

    /**
     * Gets content data.
     * @return content data
     */
    public String getData() {
      return data;
    }

    /**
     * Gets content type.
     * @return content type
     */
    public MimeType getContentType() {
      return contentType;
    }
    
    @Override
    public String toString() {
      return String.format("[%s]: %s", contentType, data);
    }
  }
}
