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
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gpt input broker.
 */
/*package*/
class GptBroker implements InputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(GptBroker.class);
  private final GptConnector connector;
  private final GptBrokerDefinitionAdaptor definition;
  private final String collectionsFieldName;

  private Client client;
  private TaskDefinition td;

  public GptBroker(GptConnector connector, GptBrokerDefinitionAdaptor definition, String collectionsFieldName) {
    this.connector = connector;
    this.definition = definition;
    this.collectionsFieldName = collectionsFieldName;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    td = context.getTask().getTaskDefinition();
    CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().setRedirectStrategy(LaxRedirectStrategy.INSTANCE).build();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      client = new Client(httpClient, definition.getHostUrl(), definition.getCredentials(), definition.getIndex(), collectionsFieldName);
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), httpClient, definition.getHostUrl());
      client = new Client(new BotsHttpClient(httpClient, bots), definition.getHostUrl(), definition.getCredentials(), definition.getIndex(), collectionsFieldName);
    }
  }

  @Override
  public void terminate() {
    if (client!=null) {
      try {
        client.close();
      } catch (IOException ex) {
        LOG.error(String.format("Error terminating broker."), ex);
      }
    }
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("GPT", definition.getHostUrl().toExternalForm(), null);
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return definition.getCredentials()==null || definition.getCredentials().isEmpty()? true: definition.getCredentials().equals(creds);
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

  @Override
  public DataContent readContent(String id) throws DataInputException {
    try {
      SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), getEntityDefinition().getLabel(), id, null, null, td.getSource().getRef(), td.getRef());
      ref.addContext(MimeType.APPLICATION_XML, readXml(id).getBytes("UTF-8"));
      return ref;
    } catch (URISyntaxException | UnsupportedEncodingException ex) {
      throw new DataInputException(this, String.format("Error reading data %s Exception: "+ex, id), ex);
    }
  }

  private DataReference readContent(String id, Date lastModified, URI sourceUri) throws DataInputException {
    try {
      String xml;
      String json;

      SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), getEntityDefinition().getLabel(), id, lastModified, sourceUri, td.getSource().getRef(), td.getRef());

      if (definition.getEmitXml() && (xml = readXml(id)) != null) {
        ref.addContext(MimeType.APPLICATION_XML, xml.getBytes("UTF-8"));
      }

      if (definition.getEmitJson() && (json = readJson(id)) != null) {
        ref.addContext(MimeType.APPLICATION_JSON, json.getBytes("UTF-8"));
      }

      return ref;
    } catch (URISyntaxException | IOException ex) {
      throw new DataInputException(GptBroker.this, String.format("Error iterating through Geoportal Server 2.0 records. Exception: "+ex), ex);
    }
  }

  private String readXml(String id) {
    try {
      return client.readXml(id);
    } catch (URISyntaxException | IOException ex) {
      return null;
    }
  }

  private String readJson(String id) {
    try {
      return client.readJson(id);
    } catch (URISyntaxException | IOException ex) {
      return null;
    }
  }

  private class GptIterator implements InputBroker.Iterator {

    private java.util.Iterator<String> iter;

    @Override
    public boolean hasNext() throws DataInputException {
      try {
        if (iter == null) {
          iter = null;
          List<String> list = client.listIds();
          if (list == null || list.isEmpty()) {
            return false;
          }
          iter = list.iterator();
        }
        return iter.hasNext();
      } catch (IOException | URISyntaxException ex) {
        throw new DataInputException(GptBroker.this, String.format("Error iterating through Geoportal Server 2.0 records. Exception: "+ex), ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      if (iter == null || !iter.hasNext()) {
        throw new DataInputException(GptBroker.this, String.format("Error iterating through Geoportal Server 2.0 records."));
      }
      String id = iter.next();
      try {
        EntryRef entryRef = client.readItem(id);
        return readContent(id, entryRef.getLastModified(), entryRef.getSourceUri());
      } catch (URISyntaxException | IOException ex) {
        throw new DataInputException(GptBroker.this, String.format("Error iterating through Geoportal Server 2.0 records."), ex);
      }
    }

  }
}
