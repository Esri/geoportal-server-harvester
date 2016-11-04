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

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.StringAttribute;
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
import java.util.HashMap;
import java.util.List;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * CKAN broker.
 */
/*package*/ class CkanBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(CkanBroker.class);
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
          resourcesIter = dataSetsIter.next().getResources().iterator();
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
        /*
          <xsl:param name="modified"/>
          <xsl:param name="resource.url"/>
          <xsl:param name="resource.url.scheme"/>
          <xsl:param name="bbox"/>
        */
        attrs.put("identifier", new StringAttribute(resource.getId()));
        attrs.put("title", new StringAttribute(resource.getName()));
        attrs.put("description", new StringAttribute(resource.getDescription()));
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
    
  }
}
