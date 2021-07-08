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

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.commons.thredds.client.Client;
import com.esri.geoportal.commons.thredds.client.Catalog;
import com.esri.geoportal.commons.thredds.client.Content;
import com.esri.geoportal.commons.thredds.client.ContentData;
import com.esri.geoportal.commons.thredds.client.Record;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.xml.sax.SAXException;

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
    CloseableHttpClient http = HttpClientBuilder.create().useSystemProperties().setRedirectStrategy(LaxRedirectStrategy.INSTANCE).build();
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

    if (client != null) {
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
    try {
      ThreddsIter iter = new ThreddsIter(this.getBrokerUri(), iteratorContext) {
        @Override
        protected void onClose() {
          iterators.remove(this);
        }
      };

      iterators.add(iter);
      return iter;
    } catch (URISyntaxException ex) {
      throw new DataInputException(this, String.format("Invalid broker uri"), ex);
    }
  }

  private class ThreddsIter implements InputBroker.Iterator {

    private final URI brokerUri;
    private final IteratorContext iteratorContext;
    private final HashSet<URL> visitedFolders = new HashSet<>();
    private LinkedList<URL> folders;
    private java.util.Iterator<Record> recIter;
    private Content nextContent;

    public ThreddsIter(URI brokerUri, IteratorContext iteratorContext) {
      this.brokerUri = brokerUri;
      this.iteratorContext = iteratorContext;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      if (nextContent != null) {
        return true;
      }

      try {
        if (folders == null) {
          Catalog content = client.readCatalog(definition.getHostUrl());
          
          folders = new LinkedList<>(selectFolders(content.folders));
          recIter = content.records.iterator();
        }

        while (!recIter.hasNext()) {
          if (Thread.currentThread().isInterrupted()) {
            return false;
          }
          if (folders.isEmpty()) {
            return false;
          }

          Catalog content = client.readCatalog(folders.pollFirst());
          
          folders.addAll(selectFolders(content.folders));
          recIter = content.records.iterator();
        }

        do {
          if (Thread.currentThread().isInterrupted()) {
            return false;
          }
          nextContent = readContent(recIter.next());
          if (nextContent!=null) {
            return true;
          }
        } while (recIter.hasNext());

        return hasNext();
      } catch (DataInputException | URISyntaxException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
        throw new DataInputException(ThreddsBroker.this, String.format("Error retrieving content."), ex);
      }
    }

    private List<URL> selectFolders(List<URL> fld) {
      fld.stream().filter(url -> visitedFolders.contains(url)).forEach(url -> {
        LOG.debug(String.format("Skipping duplicated sub-catalog: %s", url));
      });
      fld = fld.stream().filter(url -> !visitedFolders.contains(url)).collect(Collectors.toList());
      visitedFolders.addAll(fld);
      return fld;
    }

    private Content readContent(Record rec) {
      try {
        Content content = client.fetchContent(rec, preDownload -> iteratorContext.getLastHarvestDate() == null || preDownload.lastModifiedDate == null || preDownload.lastModifiedDate.getTime() >= iteratorContext.getLastHarvestDate().getTime());
        if (content != null && content.body != null) {
          return content;
        }
      } catch (IOException ignore) {
        LOG.debug(String.format("Error reading record %s (%s)", rec.id, rec.uri), ignore);
      }
      return null;
    }

    @Override
    public DataReference next() throws DataInputException {
      if (nextContent == null) {
        throw new DataInputException(ThreddsBroker.this, String.format("No more records."));
      }
      DataReference ref = makeReference(brokerUri, nextContent);
      nextContent = null;
      return ref;
    }

    protected void onClose() {
      // called upon closing iterator
    }

    private void close() {
      onClose();
    }
  }
  
  private DataReference makeReference(URI brokerUri, Content content) {
    SimpleDataReference ref = new SimpleDataReference(
      brokerUri,
      ThreddsBroker.this.getEntityDefinition().getLabel(),
      content.record.id,
      content.lastModifiedDate,
      content.record.uri,
      ThreddsBroker.this.td.getSource().getRef(),
      ThreddsBroker.this.td.getRef()
    ) {
      @Override
      public String getFetchableId() {
        return super.getSourceUri().toASCIIString();
      }
      
    };
    ref.addContext(content.contentType, content.body);
    return ref;
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
      ContentData content = client.fetchContentData(new URI(id), cont -> true);
      return new DataContent() {
        @Override
        public byte[] getContent(MimeType... mimeType) throws IOException {
          return content.body;
        }

        @Override
        public Set<MimeType> getContentType() {
          return new HashSet<>(Arrays.asList(new MimeType[]{content.contentType}));
        }
        
      };
    } catch (IOException|URISyntaxException ex) {
      throw new DataInputException(this, String.format("Error reading content for %s", id), ex);
    }
  }

}
