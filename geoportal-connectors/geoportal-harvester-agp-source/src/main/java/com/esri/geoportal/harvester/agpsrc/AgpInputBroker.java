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
import com.esri.geoportal.commons.agp.client.Group;
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
import com.esri.geoportal.commons.utils.SimpleCredentials;
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
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.impl.client.LaxRedirectStrategy;

/**
 * ArcGIS Portal output broker.
 */
/*package*/ class AgpInputBroker implements InputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(AgpInputBroker.class);
  
  private final AgpInputConnector connector;
  private final AgpInputBrokerDefinitionAdaptor definition;
  private final MetaBuilder metaBuilder;
  private AgpClient client;
  private TaskDefinition td;
 
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
  public boolean hasAccess(SimpleCredentials creds) {
    return definition.getCredentials()==null || definition.getCredentials().isEmpty()? true: definition.getCredentials().equals(creds);
  }

  @Override
  public String toString() {
    return String.format("AGPSRC [%s]", definition.getHostUrl());
  }

  private String generateToken(int minutes) throws URISyntaxException, IOException {
    return client.generateToken(minutes).token;
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
    td = context.getTask().getTaskDefinition();
    CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().setRedirectStrategy(LaxRedirectStrategy.INSTANCE).build();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      client = new AgpClient(httpclient, definition.getHostUrl(),definition.getCredentials(), definition.getMaxRedirects());
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), httpclient, definition.getHostUrl());
      client = new AgpClient(new BotsHttpClient(httpclient,bots), definition.getHostUrl(), definition.getCredentials(), definition.getMaxRedirects());
    }
    
    try {
      // validate folder id/title and change it to id if title provided
      String folderId = StringUtils.trimToNull(definition.getFolderId());
      if (folderId!=null) {
        FolderEntry[] folders = this.client.listFolders(definition.getCredentials().getUserName(), generateToken(1));
        FolderEntry selectedFodler = folders!=null? Arrays.stream(folders).filter(folder->folder.id!=null && folder.id.equals(folderId)).findFirst().orElse(
                Arrays.stream(folders).filter(folder->folder.title!=null && folder.title.replaceAll("\\s", "").equalsIgnoreCase(folderId.replaceAll("\\s", ""))).findFirst().orElse(null)
        ): null;
        if (selectedFodler!=null) {
          definition.setFolderId(selectedFodler.id);
        } else {
          definition.setFolderId(null);
        }
      } else {
        definition.setFolderId(null);
      }
      
      // validate group id/title and change it to id if title provided
      String groupId = StringUtils.trimToNull(definition.getGroupId());
      if (groupId!=null) {
        Group [] groups = this.client.listGroups(definition.getCredentials().getUserName(), generateToken(1));
        Group selectedGroup =  groups!=null? Arrays.stream(groups).filter(group->group.id!=null && group.id.equals(groupId)).findFirst().orElse(
                Arrays.stream(groups).filter(group->group.title!=null && group.title.replaceAll("\\s", "").equalsIgnoreCase(groupId.replaceAll("\\s", ""))).findFirst().orElse(null)
        ): null;
        if (selectedGroup!=null) {
          definition.setGroupId(selectedGroup.id);
        } else {
          definition.setGroupId(null);
        }
      } else {
        definition.setGroupId(null);
      }
    } catch (IOException|URISyntaxException ex) {
      throw new DataProcessorException(String.format("Error listing folders for user: %s Exception: "+ex, definition.getCredentials().getUserName()), ex);
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
  public DataContent readContent(String id) throws DataInputException {
    try {
      ItemEntry itemEntry = client.readItem(id, client.generateToken(1).token);
      SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), itemEntry.id, new Date(itemEntry.modified), URI.create(itemEntry.id), td.getSource().getRef(), td.getRef());
      ref.addContext(MimeType.APPLICATION_JSON, mapper.writeValueAsString(itemEntry).getBytes("UTF-8"));
      return ref;
    } catch (IOException|URISyntaxException ex) {
      throw new DataInputException(this, String.format("Error reading data from: %s Exception: "+ex, id), ex);
    }
  }

  private DataReference createReference(ItemEntry itemEntry) throws URISyntaxException, IOException, MetaException, TransformerException {
    Properties props = new Properties();
    if (itemEntry.id!=null) {
      props.put(WKAConstants.WKA_IDENTIFIER, itemEntry.id);
    }
    if (itemEntry.title!=null) {
      props.put(WKAConstants.WKA_TITLE, itemEntry.title);
    }
    if (itemEntry.description!=null) {
      props.put(WKAConstants.WKA_DESCRIPTION, itemEntry.description);
    }
    if (itemEntry.url!=null) {
      props.put(WKAConstants.WKA_RESOURCE_URL, itemEntry.url);
    } else if (ItemType.WEB_MAP.getTypeName().equals(itemEntry.type)) {
      props.put(WKAConstants.WKA_RESOURCE_URL, definition.getHostUrl().toExternalForm().replaceAll("/+$", "")+"/home/webmap/viewer.html?webmap="+itemEntry.id);
    } else {
      props.put(WKAConstants.WKA_RESOURCE_URL, definition.getHostUrl().toExternalForm().replaceAll("/+$", "")+"/home/item.html?id="+itemEntry.id);
    }

    if (itemEntry.extent!=null && itemEntry.extent.length==2 && itemEntry.extent[0]!=null && itemEntry.extent[0].length==2 && itemEntry.extent[1]!=null && itemEntry.extent[1].length==2) {
      String sBox = String.format("%f %f,%f %f", itemEntry.extent[0][0], itemEntry.extent[0][1], itemEntry.extent[1][0], itemEntry.extent[1][1]);
      props.put(WKAConstants.WKA_BBOX, sBox);
    }

    SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), itemEntry.id, new Date(itemEntry.modified), URI.create(itemEntry.id), td.getSource().getRef(), td.getRef());

    if (definition.getEmitXml()) {
      String orgMeta = null;
      if (Arrays.stream(itemEntry.typeKeywords).anyMatch((String a) -> a.equalsIgnoreCase("metadata")) && (orgMeta = client.readItemMetadata(itemEntry.id, AgpClient.MetadataFormat.DEFAULT, generateToken(1))) != null) {
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
      ref.addContext(MimeType.APPLICATION_JSON, mapper.writeValueAsString(itemEntry).getBytes("UTF-8"));
      
      // attributes

      if (itemEntry.tags!=null) {
        ArrayNode tagsNode = mapper.createArrayNode();
        Arrays.stream(itemEntry.tags).forEach(tag -> tagsNode.add(tag));
        ref.getAttributesMap().put("keywords_s", tagsNode);
      }
    }

    return ref;
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
          throw new DataInputException(AgpInputBroker.this, String.format("Error reading content. Exception: "+ex), ex);
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
        return createReference(nextEntry);
      } catch (MetaException|TransformerException|URISyntaxException|IOException ex) {
        throw new DataInputException(AgpInputBroker.this, String.format("Error reading next data reference. Exception: "+ex), ex);
      }
    }
    
    private List<ItemEntry> list() throws URISyntaxException, IOException {
      if (!definition.getCredentials().isEmpty()) {
        if (definition.getGroupId()==null) {
          ContentResponse content = client.listContent(definition.getCredentials().getUserName(), definition.getFolderId(), size, from, generateToken(1));
          from += size;
          return content!=null && content.items!=null && content.items.length>0? Arrays.asList(content.items): null;
        } else {
          ContentResponse content = client.listGroupContent(definition.getGroupId(), size, from, generateToken(1));
          from += size;
          return content!=null && content.items!=null && content.items.length>0? Arrays.asList(content.items): null;
        }
      } else {
        QueryResponse content = client.listPublicContent(size, from);
        from += size;
        return content!=null && content.results!=null && content.results.length>0? Arrays.asList(content.results): null;
      }
    }
    
  }
}
