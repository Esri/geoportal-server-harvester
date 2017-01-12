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
package com.esri.geoportal.harvester.ags;

import com.esri.geoportal.commons.ags.client.AgsClient;
import com.esri.geoportal.commons.ags.client.ContentResponse;
import com.esri.geoportal.commons.ags.client.ExtentInfo;
import com.esri.geoportal.commons.ags.client.LayerInfo;
import com.esri.geoportal.commons.ags.client.LayerRef;
import com.esri.geoportal.commons.ags.client.ServerResponse;
import com.esri.geoportal.commons.ags.client.ServiceInfo;
import com.esri.geoportal.commons.constants.ItemType;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.StringAttribute;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Ags broker.
 */
/*package*/ class AgsBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(AgsBroker.class);
  private static final Pattern rootPattern = Pattern.compile("\\/[^\\/]*Server(\\/[0-9]+)?$");

  private final AgsConnector connector;
  private final AgsBrokerDefinitionAdaptor definition;
  private final MetaBuilder metaBuilder;
  private AgsClient client;

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition definition
   * @param metaBuilder meta builder
   */
  public AgsBroker(AgsConnector connector, AgsBrokerDefinitionAdaptor definition, MetaBuilder metaBuilder) {
    this.connector = connector;
    this.definition = definition;
    this.metaBuilder = metaBuilder;
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
    CloseableHttpClient httpclient = HttpClients.createDefault();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      client = new AgsClient(httpclient, definition.getHostUrl());
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), httpclient, definition.getHostUrl());
      client = new AgsClient(new BotsHttpClient(httpclient,bots), definition.getHostUrl());
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

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("AGS", definition.getHostUrl().toExternalForm(), null);
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    try {
      if (iteratorContext.getLastHarvestDate()!=null) {
        LOG.info(String.format("Incremental harvest is not supported by Server for ArcGIS connector. Full harvest will be performed instead."));
      }
      List<ServerResponse> responses = listResponses(null);
      return new AgsIterator(responses.iterator());
    } catch (URISyntaxException|IOException ex) {
      throw new DataInputException(this, String.format("Error listing server content."), ex);
    }
  }

  private ServerResponse layerInfoToServerResponse(LayerInfo layerInfo) {
    ServerResponse response = new ServerResponse();
    response.url = layerInfo.url;
    response.name = layerInfo.name;
    response.description = layerInfo.description;
    response.fullExtent = layerInfo.extent;
    response.initialExtent = layerInfo.extent;
    return response;
  }
  
  private List<ServerResponse> listResponses(String rootFolder) throws URISyntaxException, IOException {
    ArrayList<ServerResponse> responses = new ArrayList<>();

    ContentResponse content = client.listContent(rootFolder);
    if (content.services != null) {
      for (ServiceInfo si : content.services) {
        ServerResponse response = client.readServiceInformation(rootFolder, si);
        responses.add(response);
        if (definition.getEnableLayers() && response.layers!=null) {
          for (LayerRef lRef: response.layers) {
            if (lRef.subLayerIds==null || !lRef.subLayerIds.isEmpty()) {
              LayerInfo layerInfo = client.readLayerInformation(rootFolder, si, lRef);
              ServerResponse rsp = layerInfoToServerResponse(layerInfo);
              responses.add(rsp);
            }
          }
        }
      }
    }
    if (content.folders != null) {
      for (String f : content.folders) {
        String subFolder = (rootFolder != null ? rootFolder + "/" : "") + f;
        List<ServerResponse> subResponses = listResponses(subFolder);
        responses.addAll(subResponses);
      }
    }

    return responses;
  }

  @Override
  public String toString() {
    return String.format("AGS [%s]", definition.getHostUrl());
  }

  /**
   * ArcGIS content iterator.
   */
  private class AgsIterator implements InputBroker.Iterator {

    private final java.util.Iterator<ServerResponse> iterator;

    public AgsIterator(java.util.Iterator<ServerResponse> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      return iterator.hasNext();
    }

    @Override
    public DataReference next() throws DataInputException {
      try {
        ServerResponse next = iterator.next();
        String serviceType = getServiceType(next.url);
        String serviceRoot = getServiceRoot(next.url);
        
        HashMap<String, Attribute> attributes = new HashMap<>();
        attributes.put(WKAConstants.WKA_IDENTIFIER, new StringAttribute(next.url));
        attributes.put(WKAConstants.WKA_TITLE, new StringAttribute(String.format("%s/%s", serviceRoot, StringUtils.defaultString(StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(next.mapName, next.name),StringUtils.defaultIfBlank(serviceType, next.url))))));
        attributes.put(WKAConstants.WKA_DESCRIPTION, new StringAttribute(StringUtils.defaultString(StringUtils.defaultIfBlank(next.description, next.serviceDescription))));
        attributes.put(WKAConstants.WKA_RESOURCE_URL, new StringAttribute(next.url));
        attributes.put(WKAConstants.WKA_RESOURCE_URL_SCHEME, new StringAttribute("urn:x-esri:specification:ServiceType:ArcGIS:" + (serviceType!=null? serviceType: "Unknown")));
        
        String sBox = createBBox(next.fullExtent);
        if (sBox==null) {
          sBox = createBBox(next.initialExtent);
        }
        if (sBox!=null) {
          attributes.put(WKAConstants.WKA_BBOX, new StringAttribute(sBox));
        }

        MapAttribute attrs = new MapAttribute(attributes);
        Document document = metaBuilder.create(attrs);

        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        
        byte [] bytes = writer.toString().getBytes("UTF-8");
        
        SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), getEntityDefinition().getLabel(),  next.url, null, URI.create(next.url), bytes, MimeType.APPLICATION_JSON);
        ref.getAttributesMap().put("attributes", attrs);
        
        return ref;
      } catch (TransformerException|TransformerFactoryConfigurationError|IOException|URISyntaxException|MetaException ex) {
        throw new DataInputException(AgsBroker.this, String.format("Error creating data reference for ArcGIS Server service"), ex);
      }
    }
    
    private String createBBox(ExtentInfo extent) {
      if (extent!=null && extent.isValid()) {
        return String.format("%f %f,%f %f", extent.xmin, extent.ymin, extent.xmax, extent.ymax);
      }
      return null;
    }
    
    private String getServiceType(String url) {
      return ItemType.matchPattern(url).stream()
              .filter(it->it.getServiceType()!=null)
              .map(ItemType::getServiceType)
              .findFirst().orElse(null);
    }
    
    private String getServiceRoot(String url) {
      if (url!=null) {
        Matcher matcher = rootPattern.matcher(url);
        if (matcher.find()) {
          int slashIndex = matcher.start();
          if (slashIndex>0) {
            url = url.substring(0,slashIndex);
            slashIndex = url.lastIndexOf("/");
            return slashIndex>=0? url.substring(slashIndex+1): url;
          }
        }
      }
      return null;
    }

  }
}
