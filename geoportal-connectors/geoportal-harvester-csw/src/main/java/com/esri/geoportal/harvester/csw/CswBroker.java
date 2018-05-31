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
package com.esri.geoportal.harvester.csw;

import com.esri.geoportal.commons.csw.client.IClient;
import com.esri.geoportal.commons.csw.client.IRecord;
import com.esri.geoportal.commons.csw.client.IRecords;
import com.esri.geoportal.commons.csw.client.impl.Client;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSW broker.
 */
/*package*/ class CswBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(CswBroker.class);
  private static final int PAGE_SIZE = 10;

  private final CswConnector connector;
  private final CswBrokerDefinitionAdaptor definition;
  private CloseableHttpClient httpclient;
  private IClient client;
  private java.util.Iterator<IRecord> recs;
  private IRecord nextRecord;
  private int start = 1;
  private boolean noMore;
  private TaskDefinition td;

  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   */
  public CswBroker(CswConnector connector, CswBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    td = context.getTask().getTaskDefinition();
    httpclient = HttpClientBuilder.create().useSystemProperties().build();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      client = new Client(httpclient, definition.getHostUrl(), definition.getProfile(), definition.getCredentials());
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), httpclient, definition.getHostUrl());
      client = new Client(new BotsHttpClient(httpclient,bots), definition.getHostUrl(), definition.getProfile(), definition.getCredentials());
    }
  }

  @Override
  public void terminate() {
    if (httpclient!=null) {
      try {
        httpclient.close();
      } catch (IOException ex) {
        LOG.error(String.format("Error terminating CswBroker."), ex);
      }
    }
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("CSW",definition.getHostUrl().toExternalForm(),definition.getProfile().getId());
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    return new CswIterator(iteratorContext);
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return definition.getCredentials()==null? true: definition.getCredentials().equals(creds);
  }

  @Override
  public String toString() {
    return String.format("CSW [%s, %s]", definition.getHostUrl(), definition.getProfile());
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
    return readContent(id, null);
  }

  private DataReference readContent(String id, Date lastModified) throws DataInputException {
    try {
      String metadata = client.readMetadata(id);
      SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), getEntityDefinition().getLabel(), id, lastModified, new URI("uuid", id, null), td.getSource().getRef(), td.getRef());
      ref.addContext(MimeType.APPLICATION_XML, metadata.getBytes("UTF-8"));
      return ref;
    } catch (Exception ex) {
      throw new DataInputException(this, String.format("Error reading data %s", id), ex);
    }
  }
  
  /**
   * CSW iterator.
   */
  private class CswIterator implements InputBroker.Iterator {
    private final IteratorContext iteratorContext;

    /**
     * Creates instance of the iterator.
     * @param iteratorContext iterator context
     */
    public CswIterator(IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }
    
    
    @Override
    public boolean hasNext() throws DataInputException {
      try {
        if (noMore) {
          return false;
        }

        if (recs==null) {
          IRecords r = client.findRecords(start, PAGE_SIZE, iteratorContext.getLastHarvestDate(), null, definition.getSearchText());
          if (r.isEmpty()) {
            noMore = true;
          } else {
            recs = r.iterator();
          }
          return hasNext();
        }

        if (!recs.hasNext()) {
          recs = null;
          start += PAGE_SIZE;
          return hasNext();
        }
        
        IRecord rec = recs.next();
        
        if (rec.getLastModifiedDate()!=null && iteratorContext.getLastHarvestDate()!=null && !(rec.getLastModifiedDate().getTime()>=iteratorContext.getLastHarvestDate().getTime())) {
          return hasNext();
        }

        nextRecord = rec;
        return true;
      } catch (Exception ex) {
        throw new DataInputException(CswBroker.this, "Error reading data.", ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      if (nextRecord==null) {
        throw new DataInputException(CswBroker.this, String.format("No more records."));
      }
      IRecord rec = nextRecord;
      nextRecord=null;
      return readContent(rec.getId(), rec.getLastModifiedDate());
    }
  }
  
}
