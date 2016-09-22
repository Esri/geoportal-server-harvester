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
package com.esri.geoportal.harvester.agp;

import com.esri.geoportal.commons.agp.client.AgpClient;
import com.esri.geoportal.commons.agp.client.DataType;
import com.esri.geoportal.commons.agp.client.DeleteResponse;
import com.esri.geoportal.commons.agp.client.ItemEntry;
import com.esri.geoportal.commons.agp.client.ItemResponse;
import com.esri.geoportal.commons.agp.client.ItemType;
import com.esri.geoportal.commons.agp.client.QueryResponse;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaAnalyzer;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.BaseProcessInstanceListener;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * ArcGIS Portal output broker.
 */
/*package*/ class AgpOutputBroker implements OutputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(AgpOutputBroker.class);
  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  private final AgpOutputConnector connector;
  private final AgpOutputBrokerDefinitionAdaptor definition;
  private final MetaAnalyzer metaAnalyzer;
  private AgpClient client;
  private String token;
  private final Set<String> existing = new HashSet<>();
  private volatile boolean preventCleanup;

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition
   * @param metaAnalyzer
   */
  public AgpOutputBroker(AgpOutputConnector connector, AgpOutputBrokerDefinitionAdaptor definition, MetaAnalyzer metaAnalyzer) {
    this.connector = connector;
    this.definition = definition;
    this.metaAnalyzer = metaAnalyzer;
  }
  
  @Override
  public PublishingStatus publish(DataReference ref) throws DataOutputException {
    try {
      // extract map of attributes (normalized names)
      MapAttribute attributes = extractMapAttributes(ref);

      if (attributes == null) {
        throw new DataOutputException(this, String.format("Error extracting attributes from data."));
      }

      // build typeKeywords array
      String src_source_type_s = URLEncoder.encode(ref.getBrokerUri().getScheme(), "UTF-8");
      String src_source_uri_s = URLEncoder.encode(ref.getBrokerUri().toASCIIString(), "UTF-8");
      String src_source_name_s = URLEncoder.encode(ref.getBrokerName(), "UTF-8");
      String src_uri_s = URLEncoder.encode(ref.getSourceUri().toASCIIString(), "UTF-8");
      String src_lastupdate_dt = ref.getLastModifiedDate() != null ? URLEncoder.encode(fromatDate(ref.getLastModifiedDate()), "UTF-8") : null;

      String[] typeKeywords = {
        String.format("src_source_type_s=%s", src_source_type_s),
        String.format("src_source_uri_s=%s", src_source_uri_s),
        String.format("src_source_name_s=%s", src_source_name_s),
        String.format("src_uri_s=%s", src_uri_s),
        String.format("src_lastupdate_dt=%s", src_lastupdate_dt)
      };
      
      try {

        // generate token
        if (token == null) {
          token = generateToken();
        }
        
        // find resource URL
        URL resourceUrl = new URL(getAttributeValue(attributes, "resource.url", null));
        ItemType itemType = ItemType.matchPattern(resourceUrl.toExternalForm()).stream().findFirst().orElse(null);
        if (itemType == null || itemType.getDataType()!=DataType.URL) {
          return PublishingStatus.SKIPPED;
        }
        
        // find thumbnail URL
        URL thumbnailUrl = new URL(getAttributeValue(attributes, "thumbnail.url", null));

        // check if item exists
        QueryResponse search = client.search(String.format("typekeywords:%s", String.format("src_uri_s=%s", src_uri_s)), 0, 0, token);
        ItemEntry itemEntry = search!=null && search.results!=null && search.results.length>0? search.results[0]: null;
        
        if (itemEntry==null) {
          // add item if doesn't exist
          ItemResponse response = addItem(
                  getAttributeValue(attributes, "title", null),
                  getAttributeValue(attributes, "description", null),
                  resourceUrl, thumbnailUrl, 
                  itemType, extractEnvelope(getAttributeValue(attributes, "bbox", null)), typeKeywords);

          if (response == null || !response.success) {
            throw new DataOutputException(this, String.format("Error adding item: %s", ref.getSourceUri()));
          }

          return PublishingStatus.CREATED;
        } else if (itemEntry.owner.equals(definition.getCredentials().getUserName())) {
          itemEntry = client.readItem(itemEntry.id, token);
          if (itemEntry==null) {
            throw new DataOutputException(this, String.format("Unable to read item entry."));
          }
          // update item if does exist
          ItemResponse response = updateItem(
                  itemEntry.id,
                  itemEntry.owner,
                  itemEntry.ownerFolder,
                  getAttributeValue(attributes, "title", null),
                  getAttributeValue(attributes, "description", null),
                  resourceUrl, thumbnailUrl,
                  itemType, extractEnvelope(getAttributeValue(attributes, "bbox", null)), typeKeywords);
          if (response == null || !response.success) {
            throw new DataOutputException(this, String.format("Error updating item: %s", ref.getSourceUri()));
          }
          existing.remove(itemEntry.id);
          return PublishingStatus.UPDATED;
        } else {
          return PublishingStatus.SKIPPED;
        }
      } catch (MalformedURLException ex) {
        return PublishingStatus.SKIPPED;
      }

    } catch (MetaException | IOException | ParserConfigurationException | SAXException | URISyntaxException ex) {
      throw new DataOutputException(this, String.format("Error publishing data"), ex);
    }
  }
  
  private Double [] extractEnvelope(String sBbox) {
    Double [] envelope = null;
    if (sBbox!=null) {
      String[] corners = sBbox.split(",");
      if (corners!=null && corners.length==2) {
        String [] minXminY = corners[0].split(" ");
        String [] maxXmaxY = corners[1].split(" ");
        if (minXminY!=null && minXminY.length==2 && maxXmaxY!=null && maxXmaxY.length==2) {
          minXminY[0] = StringUtils.trimToEmpty(minXminY[0]);
          minXminY[1] = StringUtils.trimToEmpty(minXminY[1]);
          maxXmaxY[0] = StringUtils.trimToEmpty(maxXmaxY[0]);
          maxXmaxY[1] = StringUtils.trimToEmpty(maxXmaxY[1]);

          Double minX = NumberUtils.isNumber(minXminY[0])? NumberUtils.createDouble(minXminY[0]): null;
          Double minY = NumberUtils.isNumber(minXminY[1])? NumberUtils.createDouble(minXminY[1]): null;
          Double maxX = NumberUtils.isNumber(maxXmaxY[0])? NumberUtils.createDouble(maxXmaxY[0]): null;
          Double maxY = NumberUtils.isNumber(maxXmaxY[1])? NumberUtils.createDouble(maxXmaxY[1]): null;

          if (minX!=null && minY!=null && maxX!=null && maxY!=null) {
            envelope = new Double[]{minX,minY,maxX,maxY};
          }
        }
      }
    }
    return envelope;
  }
  
  private String getAttributeValue(MapAttribute attributes, String attributeName, String defaultValue) {
    Attribute attr = attributes.getNamedAttributes().get(attributeName);
    return attr != null ? attr.getValue() : defaultValue;
  }

  private MapAttribute extractMapAttributes(DataReference ref) throws MetaException, IOException, ParserConfigurationException, SAXException {
      MapAttribute attributes = ref.getAttributesMap().values().stream()
              .filter(o -> o instanceof MapAttribute)
              .map(o -> (MapAttribute) o)
              .findFirst()
              .orElse(null);
      if (attributes == null) {
        Document doc = ref.getAttributesMap().values().stream()
                .filter(o -> o instanceof Document)
                .map(o -> (Document) o)
                .findFirst()
                .orElse(null);
        if (doc != null) {
          attributes = metaAnalyzer.extract(doc);
        } else {
          String sXml = new String(ref.getContent(), "UTF-8");
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setNamespaceAware(true);
          DocumentBuilder builder = factory.newDocumentBuilder();
          doc = builder.parse(new InputSource(new StringReader(sXml)));
          attributes = metaAnalyzer.extract(doc);
        }
      }
      return attributes;
  }

  private ItemResponse addItem(String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double [] envelope, String[] typeKeywords) throws IOException, URISyntaxException {
    if (token==null) {
      token = generateToken();
    }
    ItemResponse response = addItem(
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, token);
    if (response.error != null && response.error.code == 498) {
      token = generateToken();
      response = addItem(
              title,
              description,
              url, thumbnailUrl,
              itemType, envelope, typeKeywords, token);
    }
    return response;
  }

  private ItemResponse addItem(String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double [] envelope, String[] typeKeywords, String token) throws IOException, URISyntaxException {
    return client.addItem(
            definition.getCredentials().getUserName(),
            definition.getFolderId(),
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, null, token);
  }

  private ItemResponse updateItem(String id, String owner, String folderId, String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double [] envelope, String[] typeKeywords) throws IOException, URISyntaxException {
    if (token==null) {
      token = generateToken();
    }
    ItemResponse response = updateItem(
            id,
            owner,
            folderId,
            title,
            description,
            url, thumbnailUrl, 
            itemType, envelope, typeKeywords, token);
    if (response.error != null && response.error.code == 498) {
      token = generateToken();
      response = updateItem(
              id,
              owner,
              folderId,
              title,
              description,
              url, thumbnailUrl, 
              itemType, envelope, typeKeywords, token);
    }
    return response;
  }

  private ItemResponse updateItem(String id, String owner, String folderId, String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double [] envelope, String[] typeKeywords, String token) throws IOException, URISyntaxException {
    return client.updateItem(
            owner,
            folderId,
            id,
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, null, token);
  }
  
  private DeleteResponse deleteItem(String id, String owner, String folderId) throws URISyntaxException, IOException {
    if (token==null) {
      token = generateToken();
    }
    DeleteResponse response = deleteItem(id, owner, folderId, token);
    if (response.error != null && response.error.code == 498) {
      token = generateToken();
      response = deleteItem(id, owner, folderId, token);
    }
    return response;
  }
  
  private DeleteResponse deleteItem(String id, String owner, String folderId, String token) throws URISyntaxException, IOException {
    return client.delete(id, owner, folderId, token);
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
  public OutputConnector getConnector() {
    return connector;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    this.client = new AgpClient(definition.getHostUrl(),definition.getCredentials());
    if(definition.getCleanup()) {
      context.addListener(new BaseProcessInstanceListener() {
        @Override
        public void onError(DataException ex) {
          preventCleanup = true;
        }
      });
      try {
        String src_source_uri_s = URLEncoder.encode(context.getTask().getDataSource().getBrokerUri().toASCIIString(), "UTF-8");
        QueryResponse search = client.search(String.format("typekeywords:%s", String.format("src_source_uri_s=%s", src_source_uri_s)), 0, 0, generateToken(1));
        while (search!=null && search.results!=null && search.results.length>0) {
          existing.addAll(Arrays.asList(search.results).stream().map(i->i.id).collect(Collectors.toList()));
          if (search.nextStart>0) {
            search = client.search(String.format("typekeywords:%s", String.format("src_source_uri_s=%s", src_source_uri_s)), 0, search.nextStart, generateToken(1));
          } else {
            break;
          }
        }
      } catch (URISyntaxException|IOException ex) {
        throw new DataProcessorException(String.format("Error collecting ids of existing items."), ex);
      }
    }
  }

  @Override
  public void terminate() {
    try {
      if(definition.getCleanup() && !preventCleanup) {
        token = generateToken();
        for (String id: existing) {
          ItemEntry item = client.readItem(id, token);
          deleteItem(item.id, item.owner, item.ownerFolder);
        }
      }
      client.close();
    } catch (IOException|URISyntaxException ex) {
      LOG.error(String.format("Error terminating broker."), ex);
    }
  }

  private String fromatDate(Date date) {
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    return FORMATTER.format(zonedDateTime);
  }

}
