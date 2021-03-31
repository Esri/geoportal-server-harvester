/*
 * Copyright 2020 Esri, Inc.
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
package com.esri.geoportal.harvester.stac;

import com.esri.geoportal.commons.constants.ItemType;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.constants.MimeTypeUtils;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.StringAttribute;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_IDENTIFIER;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_MODIFIED;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_RESOURCE_URL;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_TITLE;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import static com.esri.geoportal.commons.utils.UriUtils.escapeUri;
import com.esri.geoportal.commons.utils.XmlUtils;
import com.esri.geoportal.geoportal.commons.stac.client.Catalog;
import com.esri.geoportal.geoportal.commons.stac.client.Client;
import com.esri.geoportal.geoportal.commons.stac.client.Item;
import com.esri.geoportal.geoportal.commons.stac.client.Link;
import com.esri.geoportal.geoportal.commons.stac.client.ResponseWrapper;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Initializable;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

/**
 * STAC broker.
 */
public class StacBroker implements InputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(StacBroker.class);

  private final StacConnector connector;
  private final StacBrokerDefinitionAdaptor definition;
  private final MetaBuilder metaBuilder;

  protected CloseableHttpClient httpClient;
  private Client client;
  protected TaskDefinition td;

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
  public StacBroker(StacConnector connector, StacBrokerDefinitionAdaptor definition, MetaBuilder metaBuilder) {
    this.connector = connector;
    this.definition = definition;
    this.metaBuilder = metaBuilder;
  }

  @Override
  public void initialize(Initializable.InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    td = context.getTask().getTaskDefinition();
    CloseableHttpClient http = HttpClientBuilder.create().useSystemProperties().build();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      httpClient = http;
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), http, definition.getHostUrl());
      httpClient = new BotsHttpClient(http, bots);
    }
    client = new Client(httpClient);
  }

  @Override
  public void terminate() {
    if (httpClient != null) {
      try {
        httpClient.close();
      } catch (IOException ex) {
        LOG.error(String.format("Error terminating broker."), ex);
      }
    }
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("STAC", definition.getHostUrl().toExternalForm(), null);
  }

  @Override
  public InputBroker.Iterator iterator(InputBroker.IteratorContext iteratorContext) throws DataInputException {
    return new StacIterator(iteratorContext);
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return true;
  }

  @Override
  public String toString() {
    return String.format("CKAN [%s]", definition.getHostUrl());
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
   * Creates document content from dataset.
   *
   * @param item dataset
   * @return content as string.
   * @throws DataInputException if creating content fails
   */
  protected Content createContent(ResponseWrapper<Item> wrapper) throws DataInputException {

    try {
      HashMap<String, Attribute> attrs = new HashMap<>();
      String id = firstNonBlank(wrapper.data.id);
      attrs.put(WKA_IDENTIFIER, new StringAttribute(id));
      attrs.put(WKA_TITLE, new StringAttribute(id));
      
      Date date = parseIsoDate(wrapper.data.properties!=null && wrapper.data.properties.get("datetime")!=null? 
        wrapper.data.properties.get("datetime").asText(): 
        null
      );
      if (date!=null) {
        attrs.put(WKA_MODIFIED, new StringAttribute(formatIsoDate(date)));
      }
      attrs.put(WKA_RESOURCE_URL, new StringAttribute(wrapper.url.toString()));

      if (wrapper.data.bbox!=null && wrapper.data.bbox.length==4) {
        String sBox = String.format("%f %f,%f %f", wrapper.data.bbox[0], wrapper.data.bbox[1], wrapper.data.bbox[2], wrapper.data.bbox[3]);
        attrs.put(WKAConstants.WKA_BBOX, new StringAttribute(sBox));
      }

      Document document = metaBuilder.create(new MapAttribute(attrs));

      return new Content(XmlUtils.toString(document), MimeType.APPLICATION_XML, new MapAttribute(attrs));
    } catch (MetaException | TransformerException ex) {
      throw new DataInputException(StacBroker.this, String.format("Error reading data from: %s", this), ex);
    }
  }

  private String firstNonBlank(String... strs) {
    return Arrays.asList(strs).stream().filter(s -> !StringUtils.isBlank(s)).findFirst().orElse(null);
  }

  @Override
  public DataContent readContent(String id) throws DataInputException {
    try {
      ResponseWrapper<Item> itemWrapper = client.readItem(new URL(id));
      Item item = itemWrapper.data;

      Content content = createContent(itemWrapper);

      Date date = parseIsoDate(item.properties!=null && item.properties.get("datetime")!=null? item.properties.get("datetime").asText(): null);
      SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), id, date, URI.create(escapeUri(id)), td.getSource().getRef(), td.getRef());
      if (Arrays.asList(new MimeType[]{MimeType.APPLICATION_JSON}).contains(content.getContentType())) {
        ref.addContext(MimeType.APPLICATION_JSON, content.getData().getBytes("UTF-8"));
      } else {
        ref.addContext(MimeType.APPLICATION_JSON, mapper.writeValueAsString(itemWrapper).getBytes("UTF-8"));
      }
      

      return ref;
    } catch (IOException | URISyntaxException ex) {
      throw new DataInputException(this, String.format("Error reading data from: %s", id), ex);
    }
  }

  private String generateSchemeName(String url) {
    String serviceType = url != null ? ItemType.matchPattern(url).stream()
            .filter(it -> it.getServiceType() != null)
            .map(ItemType::getServiceType)
            .findFirst().orElse(null) : null;
    if (serviceType != null) {
      return "urn:x-esri:specification:ServiceType:STAC:" + serviceType;
    }
    if (url != null) {
      int idx = url.lastIndexOf(".");
      if (idx >= 0) {
        String ext = url.substring(idx + 1);
        MimeType mimeType = MimeTypeUtils.mapExtension(ext);
        if (mimeType != null) {
          return "urn:" + mimeType.getName();
        }
      }
    }
    return null;
  }

  /**
   * Parses ISO date
   *
   * @param strDate ISO date as string
   * @return date object or <code>null</code> if unable to parse date
   */
  private Date parseIsoDate(String strDate) {
    try {
      return Date.from(ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(strDate)).toInstant());
    } catch (Exception ex) {
      return null;
    }
  }
  
  private String formatIsoDate(Date date) {
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime);
  }

  private DataReference createReference(ResponseWrapper<Item> itemWrapper) throws DataInputException, URISyntaxException, UnsupportedEncodingException, JsonProcessingException {
    Item item = itemWrapper.data;
    
    String id = firstNonBlank(item.id);
    Content content = createContent(itemWrapper);
    
    Date date = parseIsoDate(item.properties!=null && item.properties.get("datetime")!=null? item.properties.get("datetime").asText(): null);
    SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), id, date, URI.create(escapeUri(id)), td.getSource().getRef(), td.getRef());

    if (definition.getEmitXml()) {
      if (Arrays.asList(new MimeType[]{MimeType.APPLICATION_XML, MimeType.TEXT_XML}).contains(content.getContentType())) {
        ref.addContext(MimeType.APPLICATION_XML, content.getData().getBytes("UTF-8"));
      }
    }

    if (definition.getEmitJson()) {
      ref.addContext(MimeType.APPLICATION_JSON, itemWrapper.raw.getBytes("UTF-8"));
    }
    
    if (item.properties!=null) {
      item.properties.entrySet().forEach(entry -> {
        String key = entry.getKey();
        // sanitize key
        key = key.replaceAll("[:.#,]+", "_").replaceAll("^_+","");
        ref.getAttributesMap().put(key, entry.getValue());
      });
    }

    return ref;
  }
  
  private static class IteratorQueue<T> implements java.util.Iterator<T> {
    private final LinkedList<java.util.Iterator<T>> iters = new LinkedList<>();
    
    public void append(java.util.Iterator<T> iter) {
      iters.addFirst(iter);
    }

    @Override
    public boolean hasNext() {
      while(iters.size()>0) {
        java.util.Iterator<T> first = iters.peekFirst();
        if (first.hasNext()) return true;
        iters.pollFirst();
      }
      return false;
    }

    @Override
    public T next() {
      return iters.peekFirst().next();
    }
    
    
  }
  
  /**
   * CKAN iterator.
   */
  private class StacIterator implements InputBroker.Iterator {

    private final InputBroker.IteratorContext iteratorContext;
    private final Set<URL> visited = new HashSet<>();

    private IteratorQueue<URL> catalogIter;
    private java.util.Iterator<URL> dataIter;
    private ResponseWrapper<Item> nextItem;

    public StacIterator(InputBroker.IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }
    
    private void loadCatalog(URL catalogUrl) throws IOException, URISyntaxException {
      LOG.debug(String.format("Loading: %s", catalogUrl));
      Catalog catalog = client.readCatalog(catalogUrl);
      
      if (LOG.isTraceEnabled()) {
        Link [] links = catalog!=null && catalog.links!=null? catalog.links: new Link[]{};
        long itemCount = Arrays.stream(links).filter(link -> "item".equals(link.rel)).count();
        long childCount = Arrays.stream(links).filter(link -> "child".equals(link.rel)).count();
        LOG.trace(String.format("Catalog: items: %d, catalogs: %d, total: %d", itemCount, childCount, links.length));
      }

      dataIter = Arrays
        .stream(catalog!=null && catalog.links!=null? catalog.links: new Link[]{})
        .filter(link -> "item".equals(link.rel))
        .map(link -> {
          try {
            return new URL(catalogUrl, link.href);
          } catch (MalformedURLException ex) {
            LOG.debug(String.format("Error evaluating item URL: catalogUrl: %s, item href: %s", catalogUrl, link.href), ex);
            return null;
          }
        })
        .filter(url -> url!=null)
        .iterator();

      java.util.Iterator<URL> subCatalogIter = Arrays
        .stream(catalog!=null && catalog.links!=null? catalog.links: new Link[]{})
        .filter(link -> "child".equals(link.rel))
        .map(link -> {
          try {
            return new URL(catalogUrl, link.href);
          } catch (MalformedURLException ex) {
            LOG.debug(String.format("Error evaluating sub-catalog URL: catalogUrl: %s, item href: %s", catalogUrl, link.href), ex);
            return null;
          }
        })
        .filter(url -> url!=null)
        .iterator();

      if (subCatalogIter.hasNext()) {
        catalogIter.append(subCatalogIter);
      }
    }
    
    @Override
    public boolean hasNext() throws DataInputException {
        if (nextItem!=null) return true;
        
        while (dataIter != null && dataIter.hasNext()) {
          URL next = dataIter.next();
          try {
            nextItem = client.readItem(next);
            if (nextItem==null || nextItem.data==null) continue;
            return true;
          } catch (IOException|URISyntaxException ex) {
            throw new DataInputException(StacBroker.this, String.format("Error accessing %s", next), ex);
          }
        }
        
        while (catalogIter != null && catalogIter.hasNext()) {
          URL next = catalogIter.next();
          try {
            if (visited.contains(next)) continue;
            visited.add(next);
            loadCatalog(next);
            return hasNext();
          } catch (IOException|URISyntaxException ex) {
            throw new DataInputException(StacBroker.this, String.format("Error accessing %s", next), ex);
          }
        }
        
        if (catalogIter==null && dataIter==null) {
          catalogIter = new IteratorQueue<>();
          URL next = definition.getHostUrl();
          try {
            loadCatalog(next);
            return hasNext();
          } catch (IOException|URISyntaxException ex) {
            throw new DataInputException(StacBroker.this, String.format("Error accessing %s", next), ex);
          }
        }

        return false;
    }

    @Override
    public DataReference next() throws DataInputException {
      try {

        ResponseWrapper<Item> dataSet = nextItem;
        nextItem = null;
        
        return createReference(dataSet);

      } catch (URISyntaxException | UnsupportedEncodingException | IllegalArgumentException | JsonProcessingException ex) {
        throw new DataInputException(StacBroker.this, String.format("Error reading data from: %s", this), ex);
      }
    }
  }

  /**
   * Content provided by the broker.
   */
  protected static final class Content {

    private final String data;
    private final MimeType contentType;
    private final MapAttribute attributes;

    /**
     * Creates instance of the content.
     *
     * @param data content data
     * @param contentType content type
     * @param attributes attributes
     */
    public Content(String data, MimeType contentType, MapAttribute attributes) {
      this.data = data;
      this.contentType = contentType;
      this.attributes = attributes;
    }

    /**
     * Gets content data.
     *
     * @return content data
     */
    public String getData() {
      return data;
    }

    /**
     * Gets content type.
     *
     * @return content type
     */
    public MimeType getContentType() {
      return contentType;
    }

    /**
     * Gets attributes.
     * @return attributes
     */
    public MapAttribute getAttributes() {
      return attributes;
    }

    @Override
    public String toString() {
      return String.format("[%s]: %s", contentType, data);
    }
  }
}
