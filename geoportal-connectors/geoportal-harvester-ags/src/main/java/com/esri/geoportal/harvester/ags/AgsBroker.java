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

import com.esri.core.geometry.MultiPoint;
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
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.esri.geoportal.commons.utils.XmlUtils;
import com.esri.geoportal.geoportal.commons.geometry.GeometryService;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Ags broker.
 */
/*package*/ class AgsBroker implements InputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(AgsBroker.class);
  private static final Pattern rootPattern = Pattern.compile("\\/[^\\/]*Server(\\/[0-9]+)?$");
  private static final ObjectMapper mapper = new ObjectMapper();
  
  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  private final AgsConnector connector;
  private final AgsBrokerDefinitionAdaptor definition;
  private final MetaBuilder metaBuilder;
  private final GeometryService gs;
  private AgsClient client;
  private TaskDefinition td;

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition definition
   * @param metaBuilder meta builder
   * @param gs geometry service
   */
  public AgsBroker(AgsConnector connector, AgsBrokerDefinitionAdaptor definition, MetaBuilder metaBuilder, GeometryService gs) {
    this.connector = connector;
    this.definition = definition;
    this.metaBuilder = metaBuilder;
    this.gs = gs;
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
      client = new AgsClient(httpclient, definition.getHostUrl());
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), httpclient, definition.getHostUrl());
      client = new AgsClient(new BotsHttpClient(httpclient, bots), definition.getHostUrl());
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
    return new URI("AGS", definition.getHostUrl().toExternalForm(), null);
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    try {
      if (iteratorContext.getLastHarvestDate() != null) {
        LOG.info(String.format("Incremental harvest is not supported by Server for ArcGIS connector. Full harvest will be performed instead."));
      }
      List<ServerResponse> responses = listResponses(null);
      return new AgsIterator(responses.iterator());
    } catch (URISyntaxException | IOException ex) {
      throw new DataInputException(this, String.format("Error listing server content. Exception: "+ex), ex);
    }
  }

  private ServerResponse layerInfoToServerResponse(LayerInfo layerInfo) {
    ServerResponse response = new ServerResponse();
    response.url = layerInfo.url;
    response.json = layerInfo.json;
    response.name = layerInfo.name;
    response.description = layerInfo.description;
    response.fullExtent = layerInfo.extent;
    response.initialExtent = layerInfo.extent;
    response.metadataXML = layerInfo.metadataXML;
    return response;
  }

  private List<ServerResponse> listResponses(String rootFolder) throws URISyntaxException, IOException {
    ArrayList<ServerResponse> responses = new ArrayList<>();

    ContentResponse content = client.listContent(rootFolder);
    if (content.services != null) {
      for (ServiceInfo si : content.services) {
        ServerResponse response = client.readServiceInformation(rootFolder, si);
        responses.add(response);
        if (definition.getEnableLayers() && response.layers != null) {
          for (LayerRef lRef : response.layers) {
            if (lRef.subLayerIds == null || !lRef.subLayerIds.isEmpty()) {
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
  public boolean hasAccess(SimpleCredentials creds) {
    return definition.getCredentials()==null || definition.getCredentials().isEmpty()? true: definition.getCredentials().equals(creds);
  }
  
  @Override
  public String toString() {
    return String.format("AGS [%s]", definition.getHostUrl());
  }

  @Override
  public DataContent readContent(String id) throws DataInputException {
    try {
      ServerResponse serverResponse = client.readServiceInformation(new URL(id));
      SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), getEntityDefinition().getLabel(), serverResponse.url, null, URI.create(serverResponse.url), td.getSource().getRef(), td.getRef());
      ref.addContext(MimeType.APPLICATION_JSON, serverResponse.json.getBytes("UTF-8"));
      return ref;
    } catch (IOException | URISyntaxException ex) {
      throw new DataInputException(this, String.format("Error reading data %s Exception: "+ex, id), ex);
    }
  }

  private String trimHtml(String val) {
    if (val!=null) {
      String parts [] = val.split("<[^>]*>");
      if (parts!=null) {
        val = Arrays.stream(parts)
          .map(part -> StringUtils.trimToNull(part))
          .filter(part -> part!=null)
          .collect(Collectors.joining(" "));
      }
    }
    return StringUtils.trimToNull(val);
  }
  
  private DataReference createReference(ServerResponse serverResponse) throws IOException, URISyntaxException, MetaException, TransformerException {
    String serviceType = getServiceType(serverResponse.url);
    String serviceRoot = getServiceRoot(serverResponse.url);

    // select title
    String itemInfoTitle = serverResponse.itemInfo!=null? serverResponse.itemInfo.title: null;
    String constructedTitle = String.format("%s/%s", serviceRoot, StringUtils.defaultString(StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(serverResponse.mapName, serverResponse.name), StringUtils.defaultIfBlank(serviceType, serverResponse.url))));
    String title = StringUtils.defaultIfBlank(itemInfoTitle, constructedTitle);
    
    // select description
    String itemInfoDescription = trimHtml(serverResponse.itemInfo!=null? serverResponse.itemInfo.description: null);
    String serverDescription = trimHtml(StringUtils.defaultString(StringUtils.defaultIfBlank(serverResponse.description, serverResponse.serviceDescription)));
    String description = StringUtils.defaultIfBlank(itemInfoDescription, serverDescription);
    String metadataXML = serverResponse.metadataXML!=null? serverResponse.metadataXML: null;
    
    HashMap<String, Attribute> attributes = new HashMap<>();
    attributes.put(WKAConstants.WKA_IDENTIFIER, new StringAttribute(serverResponse.url));
    attributes.put(WKAConstants.WKA_TITLE, new StringAttribute(title));
    attributes.put(WKAConstants.WKA_DESCRIPTION, new StringAttribute(description));
    attributes.put(WKAConstants.WKA_RESOURCE_URL, new StringAttribute(serverResponse.url));
    attributes.put(WKAConstants.WKA_RESOURCE_URL_SCHEME, new StringAttribute("urn:x-esri:specification:ServiceType:ArcGIS:" + (serviceType != null ? serviceType : "Unknown")));
    attributes.put(WKAConstants.WKA_METADATA_XML, new StringAttribute(metadataXML));

    if (serverResponse.fullExtent != null) {
      normalizeExtent(serverResponse.fullExtent, 4326);
      String sBox = createBBox(serverResponse.fullExtent);
      if (sBox != null) {
        attributes.put(WKAConstants.WKA_BBOX, new StringAttribute(sBox));
      }
    }

    Document document = null;
    byte[] bytes = null;
    
    if (metadataXML != null && !metadataXML.trim().isEmpty()) {
        bytes = metadataXML.getBytes("UTF-8");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(metadataXML)));
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList thumbnailNodes = (NodeList) xpath.compile("/metadata/Binary/Thumbnail/Data").evaluate(document, XPathConstants.NODESET);
            if (thumbnailNodes.getLength() > 0) {
                LOG.debug(thumbnailNodes.item(0).getTextContent());
            }
            
            NodeList serviceUrlNodes = (NodeList) xpath.compile("/metadata/distInfo/distributor/distorTran/onLineSrc/linkage").evaluate(document, XPathConstants.NODESET);
            if (serviceUrlNodes.getLength() > 0) {
                serviceUrlNodes.item(0).setNodeValue(serverResponse.url);
            } else {
                // no onLineSrc/linkage nodes
                
                // get /metadata
                NodeList metadataNodes = (NodeList) xpath.compile("/metadata").evaluate(document, XPathConstants.NODESET);
                
                // get /metadata/distInfo
                NodeList distinfoNodes = (NodeList) xpath.compile("/metadata/distinfo").evaluate(document, XPathConstants.NODESET);
                if (distinfoNodes.getLength() == 0) {
                    Element distinfoElement = document.createElement("distinfo");
                    metadataNodes.item(0).appendChild(distinfoElement);
                }
 
                // get /metadata/distInfo/distributor
                NodeList distributorNodes = (NodeList) xpath.compile("/metadata/distinfo/distributor").evaluate(document, XPathConstants.NODESET);
                if (distributorNodes.getLength() == 0) {
                    distinfoNodes = (NodeList) xpath.compile("/metadata/distinfo").evaluate(document, XPathConstants.NODESET);
                    Element distributorElement = document.createElement("distributor");
                    distinfoNodes.item(0).appendChild(distributorElement);
                } 
                
                // get /metadata/distInfo/distributor/distorTran
                NodeList distorTranNodes = (NodeList) xpath.compile("/metadata/distinfo/distributor/distorTran").evaluate(document, XPathConstants.NODESET);
                if (distorTranNodes.getLength() == 0) {
                    distributorNodes = (NodeList) xpath.compile("/metadata/distinfo/distributor").evaluate(document, XPathConstants.NODESET);
                    Element distorTranElement = document.createElement("distorTran");
                    distributorNodes.item(0).appendChild(distorTranElement);
                }
                
                // get /metadata/distInfo/distributor/distorTran/onLineSrc
                NodeList onLineSrcNodes = (NodeList) xpath.compile("/metadata/distinfo/distributor/distorTran/onLineSrc").evaluate(document, XPathConstants.NODESET);
                if (onLineSrcNodes.getLength() == 0) {
                    distorTranNodes = (NodeList) xpath.compile("/metadata/distinfo/distributor/distorTran").evaluate(document, XPathConstants.NODESET);
                    Element onLineSrcElement = document.createElement("onLineSrc");
                    distorTranNodes.item(0).appendChild(onLineSrcElement);
                }
                
                // get /metadata/distInfo/distributor/distorTran/onLineSrc/linkage
                NodeList linkageNodes = (NodeList) xpath.compile("/metadata/distinfo/distributor/distorTran/onLineSrc/linkage").evaluate(document, XPathConstants.NODESET);
                if (linkageNodes.getLength() == 0) {
                    onLineSrcNodes = (NodeList) xpath.compile("/metadata/distinfo/distributor/distorTran/onLineSrc").evaluate(document, XPathConstants.NODESET);
                    Element linkageElement = document.createElement("linkage");
                    linkageElement.setTextContent(serverResponse.url);
                    onLineSrcNodes.item(0).appendChild(linkageElement);
                }
            }
            
        } catch (Exception ex) {
            LOG.error(String.format("Error geting XML document. "), ex);

        }
    } else {
        MapAttribute attrs = new MapAttribute(attributes);
        document = metaBuilder.create(attrs);
    }
    bytes = XmlUtils.toString(document).getBytes("UTF-8");
    
    SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), getEntityDefinition().getLabel(), serverResponse.url, null, URI.create(serverResponse.url), td.getSource().getRef(), td.getRef());
    attributes.entrySet().forEach(entry -> {
      ref.getAttributesMap().put(entry.getKey(), entry.getValue());
    });
    if (definition.getEmitXml()) {
      ref.addContext(MimeType.APPLICATION_XML, bytes);
    }
    if (definition.getEmitJson() && serverResponse.json != null) {
      ObjectNode jsonNode = (ObjectNode) mapper.readTree(serverResponse.json);
      if (serverResponse.itemInfo!=null) {
        JsonNode itemInfoNode = mapper.valueToTree(serverResponse.itemInfo);
        if (itemInfoNode!=null) {
          jsonNode.set("itemInfo", itemInfoNode);
        }
      }
      
      serverResponse.json = mapper.writeValueAsString(jsonNode);
      ref.addContext(MimeType.APPLICATION_JSON, serverResponse.json.getBytes("UTF-8"));
      
      // attributes
      ref.getAttributesMap().put(WKAConstants.WKA_TITLE, title);
      ref.getAttributesMap().put(WKAConstants.WKA_DESCRIPTION, description);
      
      if (serverResponse.itemInfo!=null) {
        if (serverResponse.itemInfo.tags!=null) {
          ArrayNode tagsNode = mapper.createArrayNode();
          Arrays.stream(serverResponse.itemInfo.tags).forEach(tag -> tagsNode.add(tag));
          ref.getAttributesMap().put("keywords_s", tagsNode);
        }
        String accessInformation = trimHtml(serverResponse.itemInfo.accessInformation);
        if (accessInformation!=null) {
          ref.getAttributesMap().put("accessInformation_txt", accessInformation);
        }
      }
    }

    return ref;
  }

  /**
   * Normalizes extent.
   *
   * @param extent
   * @throws IOException
   * @throws URISyntaxException
   */
  private void normalizeExtent(ExtentInfo extent, int wkid) throws IOException, URISyntaxException {
    if (extent != null && extent.isValid()) {
      if (extent.spatialReference != null && extent.spatialReference.wkid != null && extent.spatialReference.wkid != 4326) {
        MultiPoint mp = new MultiPoint();
        mp.add(extent.xmin, extent.ymin);
        mp.add(extent.xmax, extent.ymax);

        mp = gs.project(mp, extent.spatialReference.wkid.intValue(), wkid);

        if (mp.getPointCount() == 2) {
          extent.xmin = mp.getPoint(0).getX();
          extent.ymin = mp.getPoint(0).getY();
          extent.xmax = mp.getPoint(1).getX();
          extent.ymax = mp.getPoint(1).getY();
          extent.spatialReference.wkid = (long) wkid;
        }
      }
    }
  }

  private String createBBox(ExtentInfo extent) {
    if (extent != null && extent.isValid()) {
      return String.format("%f %f,%f %f", extent.xmin, extent.ymin, extent.xmax, extent.ymax);
    }
    return null;
  }

  private String getServiceType(String url) {
    return ItemType.matchPattern(url).stream()
            .filter(it -> it.getServiceType() != null)
            .map(ItemType::getServiceType)
            .findFirst().orElse(null);
  }

  private String getServiceRoot(String url) {
    if (url != null) {
      Matcher matcher = rootPattern.matcher(url);
      if (matcher.find()) {
        int slashIndex = matcher.start();
        if (slashIndex > 0) {
          url = url.substring(0, slashIndex);
          slashIndex = url.lastIndexOf("/");
          return slashIndex >= 0 ? url.substring(slashIndex + 1) : url;
        }
      }
    }
    return null;
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
        ServerResponse serverResponse = iterator.next();
        return createReference(serverResponse);
      } catch (TransformerException | TransformerFactoryConfigurationError | IOException | URISyntaxException | MetaException ex) {
        throw new DataInputException(AgsBroker.this, String.format("Error creating data reference for ArcGIS Server service Exception: "+ex), ex);
      }
    }

  }
}
