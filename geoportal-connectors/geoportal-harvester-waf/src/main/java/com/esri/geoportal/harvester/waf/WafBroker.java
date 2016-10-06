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
package com.esri.geoportal.harvester.waf;

import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WAF broker.
 */
/*package*/ class WafBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(WafBroker.class);
  private final WafConnector connector;
  private final WafBrokerDefinitionAdaptor definition;
  private final Set<URL> visited = new HashSet<>();
  
  private CloseableHttpClient httpClient;
  private LinkedList<WafFolder> subFolders;
  private LinkedList<WafFile> files;

  
  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   */
  public WafBroker(WafConnector connector, WafBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    CloseableHttpClient client = HttpClients.createDefault();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      httpClient = client;
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), client, definition.getBotsMode(), definition.getHostUrl());
      httpClient = new BotsHttpClient(client,bots);
    }
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
    return new URI("WAF",definition.getHostUrl().toExternalForm(),null);
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    return new WafIterator(iteratorContext);
  }

  @Override
  public String toString() {
    return String.format("WAF [%s]", definition.getHostUrl());
  }

  @Override
  public InputConnector getConnector() {
    return connector;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  /**
   * WAF iterator.
   */
  private class WafIterator implements InputBroker.Iterator {
    private final IteratorContext iteratorContext;
    private DataReference nextFile;

    /**
     * Creates instance of the iterator.
     * @param iteratorContext iterator context
     */
    public WafIterator(IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }
    
    
    @Override
    public boolean hasNext() throws DataInputException {

      try {
        if (files!=null && !files.isEmpty()) {
          nextFile = readContent();
          if (nextFile.getContent()==null) {
            nextFile = null;
            return hasNext();
          }
          return true;
        }

        if (subFolders!=null && !subFolders.isEmpty()) {
          WafFolder subFolder = subFolders.poll();
          if (visited.contains(subFolder.getFolderUrl())) {
            return hasNext();
          }
          visited.add(subFolder.getFolderUrl());
          WafFolderContent content = subFolder.readContent(httpClient);
          content.getSubFolders().forEach(f->subFolders.offer(f));
          files = new LinkedList<>(content.getFiles());
          return hasNext();
        }

        if (subFolders==null) {
          URL startUrl = new URL(definition.getHostUrl().toExternalForm().replaceAll("/$", "")+"/");
          WafFolderContent content = new WafFolder(WafBroker.this, startUrl, definition.getPattern(), definition.getCredentials()).readContent(httpClient);
          subFolders = new LinkedList<>(content.getSubFolders());
          files = new LinkedList<>(content.getFiles());
          return hasNext();
        }

        return false;
      } catch (IOException|URISyntaxException ex) {
        throw new DataInputException(WafBroker.this, "Error reading data.", ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      if (nextFile==null) {
        throw new DataInputException(WafBroker.this, String.format("No more records."));
      }
      DataReference result = nextFile;
      nextFile=null;
      return result;
    }
    
    private DataReference readContent() throws IOException, URISyntaxException {
      WafFile file = files.poll();
      return file.readContent(httpClient, iteratorContext.getLastHarvestDate());
    }
  }
}
