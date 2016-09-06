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
import com.esri.geoportal.commons.agp.client.ItemResponse;
import com.esri.geoportal.commons.agp.client.ItemType;
import com.esri.geoportal.commons.agp.client.QueryResponse;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaAnalyzer;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

  /**
   * Creates instance of the broker.
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
      MapAttribute attributes = ref.getAttributesMap().values().stream().filter(o->o instanceof MapAttribute).map(o->(MapAttribute)o).findFirst().get();
      if (attributes==null) {
        Document doc = ref.getAttributesMap().values().stream().filter(o->o instanceof Document).map(o->(Document)o).findFirst().get();
        if (doc!=null) {
          attributes = metaAnalyzer.extract(doc);
        } else {
          String sXml = new String(ref.getContent(),"UTF-8");
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          DocumentBuilder builder = factory.newDocumentBuilder();
          doc = builder.parse(new InputSource(new StringReader(sXml))); 
          attributes = metaAnalyzer.extract(doc);
        }
      }
      
      if (attributes==null) {
        throw new DataOutputException(this, String.format("Error extracting attributes from data."));
      }
      
      String src_source_type_s = ref.getBrokerUri().getScheme();
      String src_source_uri_s = ref.getBrokerUri().toASCIIString();
      String src_source_name_s = ref.getBrokerName();
      String src_uri_s = ref.getSourceUri().toASCIIString();
      String src_lastupdate_dt = ref.getLastModifiedDate() != null ? fromatDate(ref.getLastModifiedDate()) : null;

      String [] typeKeywords = {
        String.format("src_source_type_s_%s", src_source_type_s),
        String.format("src_source_uri_s_%s", src_source_uri_s),
        String.format("src_source_name_s_%s", src_source_name_s),
        String.format("src_uri_s_%s", src_uri_s),
        String.format("src_lastupdate_dt_%s", src_lastupdate_dt)
      };
      
      if (token==null) {
        token = client.generateToken(0, definition.getCredentials()).token;
      }
      try {
        URL url = new URL(attributes.getNamedAttributes().get("resource.url").getValue());
        ItemType itemType = ItemType.matchPattern(url.toExternalForm()).stream().findFirst().get();
        if (itemType==null) {
          return PublishingStatus.SKIPPED;
        }
      
        QueryResponse search = client.search(String.format("typeKeywords:%s", String.format("src_uri_s_%s", src_uri_s)), 0, 0, token);
        if (search.results==null || search.results.length==0) {
          ItemResponse response = client.addItem(
                  definition.getCredentials().getUserName(),
                  definition.getFolderId(),
                  attributes.getNamedAttributes().get("title").getValue(),
                  attributes.getNamedAttributes().get("description").getValue(),
                  new URL(attributes.getNamedAttributes().get("resource.url").getValue()), 
                  itemType, typeKeywords, token);
          return response!=null && response.success? PublishingStatus.CREATED: PublishingStatus.SKIPPED;
        } else {
          ItemResponse response = client.updateItem(
                  definition.getCredentials().getUserName(),
                  definition.getFolderId(),
                  search.results[0].id,
                  attributes.getNamedAttributes().get("title").getValue(),
                  attributes.getNamedAttributes().get("description").getValue(),
                  new URL(attributes.getNamedAttributes().get("resource.url").getValue()), 
                  itemType, typeKeywords, token);
          return response!=null && response.success? PublishingStatus.UPDATED: PublishingStatus.SKIPPED;
        }
      } catch (MalformedURLException ex) {
        return PublishingStatus.SKIPPED;
      }
      
    } catch (MetaException|IOException|ParserConfigurationException|SAXException|URISyntaxException ex) {
      throw new DataOutputException(this, String.format("Error publishing data"), ex);
    }
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
    this.client = new AgpClient(definition.getHostUrl());
  }

  @Override
  public void terminate() {
    try {
      client.close();
    } catch (IOException ex) {
      LOG.error(String.format("Error terminating broker."), ex);
    }
  }

  private String fromatDate(Date date) {
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    return FORMATTER.format(zonedDateTime);
  }
  
}
