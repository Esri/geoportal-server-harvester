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
package com.esri.geoportal.harvester.agpsrc;

import com.esri.geoportal.commons.agp.client.AgpClient;
import com.esri.geoportal.commons.agp.client.ContentResponse;
import com.esri.geoportal.commons.agp.client.FolderEntry;
import com.esri.geoportal.commons.agp.client.ItemEntry;
import com.esri.geoportal.commons.agp.client.QueryResponse;
import com.esri.geoportal.commons.constants.ItemType;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.meta.AttributeUtils;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Initializable.InitContext;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import com.esri.geoportal.commons.utils.XmlUtils;

/**
 * ArcGIS Portal output broker.
 */
/*package*/ class AgpInputBroker implements InputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(AgpInputBroker.class);
  
  private final AgpInputConnector connector;
  private final AgpInputBrokerDefinitionAdaptor definition;
  private final MetaBuilder metaBuilder;
  private AgpClient client;
 
  private static final ObjectMapper mapper = new ObjectMapper();
  static {
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }
  
  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition definition
   * @param metaBuilder meta builder
   */
  public AgpInputBroker(AgpInputConnector connector, AgpInputBrokerDefinitionAdaptor definition, MetaBuilder metaBuilder) {
    this.connector = connector;
    this.definition = definition;
    this.metaBuilder = metaBuilder;
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    return new AgpIterator(iteratorContext);
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("AGP", definition.getHostUrl().toExternalForm(), null);
  }

  @Override
  public String toString() {
    return String.format("AGPSRC [%s]", definition.getHostUrl());
  }

  private String generateToken(int minutes) throws URISyntaxException, IOException {
    return client.generateToken(minutes).token;
  }

  private String generateToken() throws URISyntaxException, IOException {
    return client.generateToken(60).token;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  @Override
  public InputConnector getConnector() {
    return connector;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      client = new AgpClient(httpclient, definition.getHostUrl(),definition.getCredentials());
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), httpclient, definition.getHostUrl());
      client = new AgpClient(new BotsHttpClient(httpclient,bots), definition.getHostUrl(), definition.getCredentials());
    }
    
    try {
      String folderId = StringUtils.trimToNull(definition.getFolderId());
      if (folderId!=null) {
        FolderEntry[] folders = this.client.listFolders(definition.getCredentials().getUserName(), generateToken(1));
        FolderEntry selectedFodler = Arrays.stream(folders).filter(folder->folder.id!=null && folder.id.equals(folderId)).findFirst().orElse(
                Arrays.stream(folders).filter(folder->folder.title!=null && folder.title.equals(folderId)).findFirst().orElse(null)
        );
        if (selectedFodler!=null) {
          definition.setFolderId(selectedFodler.id);
        } else {
          definition.setFolderId(null);
        }
      } else {
        definition.setFolderId(null);
      }
    } catch (IOException|URISyntaxException ex) {
      throw new DataProcessorException(String.format("Error listing folders for user: %s", definition.getCredentials().getUserName()), ex);
    }
  }

  @Override
  public void terminate() {
    try {
      client.close();
    } catch (IOException ex) {
      LOG.error(String.format("Error terminating broker."), ex);
    }
  }

  private class AgpIterator implements InputBroker.Iterator {
    private final IteratorContext iteratorContext;
    private final TransformerFactory tf = TransformerFactory.newInstance();
    private final long size = 10;
    private long from = 1;
    private java.util.Iterator<ItemEntry> iter;
    private ItemEntry nextEntry;
    private boolean done;

    public AgpIterator(IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      if (done) {
        return false;
      }
      if (iter==null) {
        try {
          List<ItemEntry> list = list();
          if (list==null || list.isEmpty()) {
            done = true;
            return false;
          }
          iter = list.iterator();
        } catch (IOException|URISyntaxException ex) {
          done = true;
          throw new DataInputException(AgpInputBroker.this, String.format("Error reading content."), ex);
        }
      }
      if (iter!=null && !iter.hasNext()) {
        iter = null;
        return hasNext();
      }
      
      nextEntry = iter.next();
      if (iteratorContext.getLastHarvestDate()!=null && nextEntry.modified<iteratorContext.getLastHarvestDate().getTime()) {
        nextEntry = null;
        return hasNext();
      }
      
      return true;
    }

    @Override
    public DataReference next() throws DataInputException {
      try {
        if (nextEntry==null) {
            throw new DataInputException(AgpInputBroker.this, String.format("Error reading content."));
        }
        
        Properties props = new Properties();
        if (nextEntry.id!=null) {
          props.put(WKAConstants.WKA_IDENTIFIER, nextEntry.id);
        }
        if (nextEntry.title!=null) {
          props.put(WKAConstants.WKA_TITLE, nextEntry.title);
        }
        if (nextEntry.description!=null) {
          props.put(WKAConstants.WKA_DESCRIPTION, nextEntry.description);
        }
        if (nextEntry.url!=null) {
          props.put(WKAConstants.WKA_RESOURCE_URL, nextEntry.url);
        } else if (ItemType.WEB_MAP.getTypeName().equals(nextEntry.type)) {
          props.put(WKAConstants.WKA_RESOURCE_URL, definition.getHostUrl().toExternalForm().replaceAll("/+$", "")+"/home/webmap/viewer.html?webmap="+nextEntry.id);
        } else {
          props.put(WKAConstants.WKA_RESOURCE_URL, definition.getHostUrl().toExternalForm().replaceAll("/+$", "")+"/home/item.html?id="+nextEntry.id);
        }
        
        if (nextEntry.extent!=null && nextEntry.extent.length==2 && nextEntry.extent[0]!=null && nextEntry.extent[0].length==2 && nextEntry.extent[1]!=null && nextEntry.extent[1].length==2) {
          String sBox = String.format("%f %f,%f %f", nextEntry.extent[0][0], nextEntry.extent[0][1], nextEntry.extent[1][0], nextEntry.extent[1][1]);
          props.put(WKAConstants.WKA_BBOX, sBox);
        }
        
        SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), nextEntry.id, new Date(nextEntry.modified), URI.create(nextEntry.id));
        
        if (definition.getEmitXml()) {
          String orgMeta = null;
          if (Arrays.stream(nextEntry.typeKeywords).anyMatch((String a) -> a.equalsIgnoreCase("metadata")) && (orgMeta = client.readItemMetadata(nextEntry.id, AgpClient.MetadataFormat.DEFAULT, generateToken(1))) != null) {
            // explicit metadata found
            ref.addContext(MimeType.APPLICATION_XML, orgMeta.getBytes("UTF-8"));
          } else {
            // generate metadata from properties
            MapAttribute attr = AttributeUtils.fromProperties(props);
            Document document = metaBuilder.create(attr);
            byte [] bytes = XmlUtils.toString(document).getBytes("UTF-8");
            ref.addContext(MimeType.APPLICATION_XML, bytes);
          }
        }
        
        if (definition.getEmitJson()) {
          ref.addContext(MimeType.APPLICATION_JSON, mapper.writeValueAsString(nextEntry).getBytes("UTF-8"));
        }
        
        return ref;
        
      } catch (MetaException|TransformerException|URISyntaxException|IOException ex) {
        throw new DataInputException(AgpInputBroker.this, String.format("Error reading next data reference."), ex);
      }
    }
    
    private List<ItemEntry> list() throws URISyntaxException, IOException {
      if (!definition.getCredentials().isEmpty()) {
        ContentResponse content = client.listContent(definition.getCredentials().getUserName(), definition.getFolderId(), size, from, generateToken(1));
        from += size;
        return content!=null && content.items!=null && content.items.length>0? Arrays.asList(content.items): null;
      } else {
        QueryResponse content = client.listPublicContent(size, from);
        from += size;
        return content!=null && content.results!=null && content.results.length>0? Arrays.asList(content.results): null;
      }
    }
    
  }
}
