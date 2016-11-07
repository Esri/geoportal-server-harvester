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
package com.esri.geoportal.geoportal.harvester.ckan;

import com.esri.geoportal.commons.constants.ItemType;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.StringAttribute;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_DESCRIPTION;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_IDENTIFIER;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_MODIFIED;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_RESOURCE_URL;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_RESOURCE_URL_SCHEME;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_TITLE;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.SearchResults;
import eu.trentorise.opendata.jackan.exceptions.JackanException;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * CKAN broker.
 */
/*package*/ class CkanBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(CkanBroker.class);
  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
  
  private final CkanConnector connector;
  private final CkanBrokerDefinitionAdaptor definition;
  private final MetaBuilder metaBuilder;
  
  private CkanClient client;
 
  
  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   * @param metaBuilder meta builder
   */
  public CkanBroker(CkanConnector connector, CkanBrokerDefinitionAdaptor definition, MetaBuilder metaBuilder) {
    this.connector = connector;
    this.definition = definition;
    this.metaBuilder = metaBuilder;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    client = definition.getToken()!=null && !definition.getToken().isEmpty()? 
              new CkanClient(definition.getHostUrl().toExternalForm(), definition.getToken())
            : new CkanClient(definition.getHostUrl().toExternalForm());
  }

  @Override
  public void terminate() {
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("CKAN",definition.getHostUrl().toExternalForm(),null);
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    return new CkanIterator(iteratorContext);
  }

  @Override
  public String toString() {
    return String.format("WAF [%s]", definition.getHostUrl());
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
   * CKAN iterator.
   */
  private class CkanIterator implements InputBroker.Iterator {
    private final IteratorContext iteratorContext;
    private final TransformerFactory tf = TransformerFactory.newInstance();
    
    private java.util.Iterator<CkanDataset> dataSetsIter;
    private CkanDataset dataSet;
    private java.util.Iterator<CkanResource> resourcesIter;
    
    private final int limit = 10;
    private int offset = 0;

    public CkanIterator(IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      try {
        if (resourcesIter!=null && resourcesIter.hasNext()) {
          return true;
        }
        
        if (dataSetsIter!=null && dataSetsIter.hasNext()) {
          dataSet = dataSetsIter.next();
          resourcesIter = dataSet.getResources().iterator();
          return hasNext();
        }

        SearchResults<CkanDataset> searchDatasets = client.searchDatasets("", limit, offset+=limit);
        List<CkanDataset> results = searchDatasets.getResults();
        
        if (results.size()>0) {
          dataSetsIter = searchDatasets.getResults().iterator();
          return hasNext();
        }
        
        return false;
      } catch (JackanException ex) {
        throw new DataInputException(CkanBroker.this, String.format("Error reading data from: %s", this), ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      try {
        CkanResource resource = resourcesIter.next();
        HashMap<String,Attribute> attrs = new HashMap<>();
        attrs.put(WKA_IDENTIFIER, new StringAttribute(firstNonBlank(resource.getId(),dataSet.getId())));
        attrs.put(WKA_TITLE, new StringAttribute(firstNonBlank(resource.getName(),dataSet.getTitle(),dataSet.getName())));
        attrs.put(WKA_DESCRIPTION, new StringAttribute(firstNonBlank(resource.getDescription())));
        attrs.put(WKA_MODIFIED, new StringAttribute(dataSet.getMetadataModified()!=null? formatIsoDate(dataSet.getMetadataModified()): ""));
        attrs.put(WKA_RESOURCE_URL, new StringAttribute(resource.getUrl()));
        String schemeName = generateSchemeName(resource.getUrl());
        if (schemeName!=null) {
          attrs.put(WKA_RESOURCE_URL_SCHEME, new StringAttribute(schemeName));
        }
        Document doc = metaBuilder.create(new MapAttribute(attrs));
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);

        return new SimpleDataReference(getBrokerUri(), definition.getEntityDefinition().getLabel(), resource.getId(), resource.getCreated(), URI.create(resource.getId()), writer.toString().getBytes("UTF-8"), MimeType.APPLICATION_XML);
        
      } catch (MetaException|TransformerException|URISyntaxException|UnsupportedEncodingException ex) {
        throw new DataInputException(CkanBroker.this, String.format("Error reading data from: %s", this), ex);
      }
    }
    
    private String firstNonBlank(String...strs) {
      return Arrays.asList(strs).stream().filter(s->!StringUtils.isBlank(s)).findFirst().orElse(null);
    }
    
  }
    
  private String generateSchemeName(String url) {
    String serviceType = ItemType.matchPattern(url).stream()
            .filter(it->it.getServiceType()!=null)
            .map(ItemType::getServiceType)
            .findFirst().orElse(null);
    if (serviceType!=null) {
      return "urn:x-esri:specification:ServiceType:ArcGIS:"+serviceType;
    }
    HashSet<MimeType> mimes = new HashSet<>();
    ItemType.matchPattern(url).stream()
            .filter(it->it.getServiceType()==null)
            .map(ItemType::getMimeTypes)
            .forEach(a->Arrays.asList(a).stream().forEach(mimes::add));
    MimeType mime = mimes.stream().findFirst().orElse(null);
    return mime!=null? mime.getName(): null;
  }

  private String formatIsoDate(Date date) {
    Instant instant = date.toInstant();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    ZoneOffset zoneOffset = ZoneOffset.ofHours(cal.getTimeZone().getRawOffset() / (1000 * 60 * 60));
    OffsetDateTime ofInstant = OffsetDateTime.ofInstant(instant, zoneOffset);
    return FORMATTER.format(ofInstant);
  }
}
