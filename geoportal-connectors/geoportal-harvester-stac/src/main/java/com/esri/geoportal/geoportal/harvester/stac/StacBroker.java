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
package com.esri.geoportal.geoportal.harvester.stac;

import com.esri.geoportal.commons.constants.ItemType;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.constants.MimeTypeUtils;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.StringAttribute;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_IDENTIFIER;
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
import com.esri.geoportal.geoportal.commons.stac.client.Relation;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
   * @param dataSet dataset
   * @return content as string.
   * @throws DataInputException if creating content fails
   */
  protected Content createContent(Item dataSet) throws DataInputException {

    try {
      HashMap<String, Attribute> attrs = new HashMap<>();
      String id = firstNonBlank(dataSet.id);
      attrs.put(WKA_IDENTIFIER, new StringAttribute(id));
      // TODO evaluate title
      attrs.put(WKA_TITLE, new StringAttribute(id));
      // TODO evaluate desription
//      attrs.put(WKA_DESCRIPTION, new StringAttribute(firstNonBlank(dataSet.notes)));
      // TODO evaluate date
//      attrs.put(WKA_MODIFIED, new StringAttribute(dataSet.metadata_modified));

      // TODO evaluate resource url
//      if (dataSet.resources != null) {
//        final List<Attribute> references = new ArrayList<>();
//        dataSet.resources.forEach(resource -> {
//          HashMap<String, Attribute> reference = new HashMap<>();
//          if (resource.url != null) {
//            String scheme = generateSchemeName(resource.url);
//            reference.put(WKA_RESOURCE_URL, new StringAttribute(resource.url));
//            if (scheme != null) {
//              reference.put(WKA_RESOURCE_URL_SCHEME, new StringAttribute(scheme));
//            }
//          }
//          references.add(new MapAttribute(reference));
//        });
//        if (references.size() == 1) {
//          Map<String, Attribute> namedAttributes = references.get(0).getNamedAttributes();
//          attrs.put(WKA_RESOURCE_URL, namedAttributes.get(WKA_RESOURCE_URL));
//          if (namedAttributes.containsKey(WKA_RESOURCE_URL_SCHEME)) {
//            attrs.put(WKA_RESOURCE_URL_SCHEME, namedAttributes.get(WKA_RESOURCE_URL_SCHEME));
//          }
//        } else if (!references.isEmpty()) {
//          attrs.put(WKA_REFERENCES, new ArrayAttribute(references));
//        }
//      }

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
      Item item = client.readItem(new URL(id));

      Content content = createContent(item);

      // TODO parse date
//      SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), id, parseIsoDate(dataSet.metadata_modified), URI.create(id), td.getSource().getRef(), td.getRef());
      SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), id, null, URI.create(id), td.getSource().getRef(), td.getRef());
      if (Arrays.asList(new MimeType[]{MimeType.APPLICATION_JSON}).contains(content.getContentType())) {
        ref.addContext(MimeType.APPLICATION_JSON, content.getData().getBytes("UTF-8"));
      } else {
        ref.addContext(MimeType.APPLICATION_JSON, mapper.writeValueAsString(item).getBytes("UTF-8"));
      }
      
      if (content.getAttributes()!=null) {
        ref.getAttributesMap().put("properties", content.getAttributes());
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
  protected Date parseIsoDate(String strDate) {
    try {
      return Date.from(ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(strDate)).toInstant());
    } catch (Exception ex) {
      return null;
    }
  }

  private DataReference createReference(Item dataSet) throws DataInputException, URISyntaxException, UnsupportedEncodingException, JsonProcessingException {
    String id = firstNonBlank(dataSet.id);
    Content content = createContent(dataSet);
    
    // TODO evaluate date
//    SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), id, parseIsoDate(dataSet.metadata_modified), URI.create(escapeUri(id)), td.getSource().getRef(), td.getRef());
    SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), id, null, URI.create(escapeUri(id)), td.getSource().getRef(), td.getRef());

    if (definition.getEmitXml()) {
      if (Arrays.asList(new MimeType[]{MimeType.APPLICATION_XML, MimeType.TEXT_XML}).contains(content.getContentType())) {
        ref.addContext(MimeType.APPLICATION_XML, content.getData().getBytes("UTF-8"));
      }
    }

    if (definition.getEmitJson()) {
      if (Arrays.asList(new MimeType[]{MimeType.APPLICATION_JSON}).contains(content.getContentType())) {
        ref.addContext(MimeType.APPLICATION_JSON, content.getData().getBytes("UTF-8"));
      } else {
        ref.addContext(MimeType.APPLICATION_JSON, mapper.writeValueAsString(dataSet).getBytes("UTF-8"));
      }
    }
    
    if (content.getAttributes()!=null) {
      ref.getAttributesMap().put("properties",content.getAttributes());
    }

    return ref;
  }
  
  public class IteratorJoin<T> implements java.util.Iterator<T> {
    private final java.util.Iterator<T> first, next;

    public IteratorJoin(java.util.Iterator<T> first, java.util.Iterator<T> next) {
        this.first = first;
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return first.hasNext() || next.hasNext();
    }

    @Override
    public T next() {
        if (first.hasNext())
            return first.next();
        return next.next();
    }
}
  /**
   * CKAN iterator.
   */
  private class StacIterator implements InputBroker.Iterator {

    private final InputBroker.IteratorContext iteratorContext;

    private java.util.Iterator<URL> catalogIter;
    private java.util.Iterator<URL> dataIter;
    private Item nextItem;

    public StacIterator(InputBroker.IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }
    
    private void loadCatalog(URL catalogUrl) throws IOException, URISyntaxException {
      Catalog catalog = client.readCatalog(catalogUrl);

      dataIter = Arrays
        .stream(catalog!=null && catalog.links!=null? catalog.links: new Link[]{})
        .filter(link -> link.rel==Relation.item)
        .map(link -> {
          try {
            return new URL(link.href);
          } catch (MalformedURLException ex) {
            return null;
          }
        })
        .filter(url -> url!=null)
        .iterator();

      java.util.Iterator<URL> subCatalogIter = Arrays
        .stream(catalog!=null && catalog.links!=null? catalog.links: new Link[]{})
        .filter(link -> link.rel==Relation.child)
        .map(link -> {
          try {
            return new URL(link.href);
          } catch (MalformedURLException ex) {
            return null;
          }
        })
        .filter(url -> url!=null)
        .iterator();

      catalogIter = catalogIter!=null? new IteratorJoin<>(catalogIter, subCatalogIter): subCatalogIter;
    }
    
    @Override
    public boolean hasNext() throws DataInputException {
        if (nextItem!=null) return true;
        
        while (dataIter != null && dataIter.hasNext()) {
          try {
            nextItem = client.readItem(dataIter.next());
            return true;
          } catch (IOException|URISyntaxException ex) {
          }
        }
        
        while (catalogIter != null && catalogIter.hasNext()) {
          try {
            loadCatalog(catalogIter.next());
            return hasNext();
          } catch (IOException|URISyntaxException ex) {
          }
        }
        
        try {
          loadCatalog(definition.getHostUrl());
        } catch (IOException|URISyntaxException ex) {
          return false;
        }

        return hasNext();
    }

    @Override
    public DataReference next() throws DataInputException {
      try {

        Item dataSet = nextItem;
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
