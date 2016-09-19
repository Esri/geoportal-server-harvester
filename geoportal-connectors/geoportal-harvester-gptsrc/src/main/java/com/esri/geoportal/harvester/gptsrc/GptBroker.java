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
package com.esri.geoportal.harvester.gptsrc;

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.gpt.client.Client;
import com.esri.geoportal.commons.gpt.client.EntryRef;
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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gpt input broker.
 */
/*package*/class GptBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(GptBroker.class);
  private final GptConnector connector;
  private final GptBrokerDefinitionAdaptor definition;

  private Client client;
  
  public GptBroker(GptConnector connector, GptBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }
  

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    client = new Client(definition.getHostUrl(), null);
  }

  @Override
  public void terminate() {
    try {
      client.close();
    } catch (IOException ex) {
      LOG.error(String.format("Error terminating broker."), ex);
    }
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("GPT",definition.getHostUrl().toExternalForm(),null);
  }

  @Override
  public String toString() {
    return String.format("GPT [%s]", definition.getHostUrl());
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
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    return new GptIterator();
  }
  
  private class GptIterator implements InputBroker.Iterator {
    private long from = 0;
    private final long size = 100;
    private java.util.Iterator<EntryRef> iter;
    private boolean done;

    @Override
    public boolean hasNext() throws DataInputException {
      try {
        if (done) return false;
        if (iter==null || !iter.hasNext()) {
          iter = null;
          List<EntryRef> list = client.list(from, size);
          from += size;
          if (list==null || list.isEmpty()) {
            done = true;
            return false;
          }
          iter = list.iterator();
        }
        return true;
      } catch (IOException|URISyntaxException ex) {
        throw new DataInputException(GptBroker.this, String.format("Error iterating through Geoportal Server 2.0 records."), ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      if (iter==null || !iter.hasNext()) {
        throw new DataInputException(GptBroker.this, String.format("Error iterating through Geoportal Server 2.0 records."));
      }
      EntryRef ref = iter.next();
      try {
        String xml = client.read(ref.getId());
        return new SimpleDataReference(getBrokerUri(), getEntityDefinition().getLabel(), ref.getId(), ref.getLastModified(), ref.getSourceUri(), xml.getBytes("UTF-8"), MimeType.APPLICATION_XML);
      } catch (URISyntaxException|IOException ex) {
        throw new DataInputException(GptBroker.this, String.format("Error iterating through Geoportal Server 2.0 records."), ex);
      }
    }
    
  }
}
