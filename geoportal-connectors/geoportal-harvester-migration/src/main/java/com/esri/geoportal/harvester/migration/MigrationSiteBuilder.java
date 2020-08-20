/*
 * Copyright 2017 Esri, Inc.
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
package com.esri.geoportal.harvester.migration;

import com.esri.geoportal.commons.csw.client.impl.Profiles;
import com.esri.geoportal.commons.csw.client.impl.ProfilesLoader;
import com.esri.geoportal.commons.csw.client.impl.ProfilesProvider;
import com.esri.geoportal.harvester.ags.AgsBrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.ckan.CkanBrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.csw.CswBrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.engine.services.Engine;
import com.esri.geoportal.harvester.engine.utils.BrokerReference;
import com.esri.geoportal.harvester.waf.WafBrokerDefinitionAdaptor;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Migration harvest site builder.
 */
/*package*/ class MigrationSiteBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(MigrationSiteBuilder.class);
  private static Profiles profiles = new Profiles();
  
  static {
    try {
      ProfilesLoader profilesLoader = new ProfilesLoader();
      profiles = profilesLoader.load();
    } catch (Exception ex) {
      LOG.warn(String.format("Error loading CSW profiles."), ex);
    }
  }

  private final Engine engine;

  private final Map<String, AdaptorDefinitonBuilder> builders = new HashMap<>();

  {
    builders.put("WAF", new WafDefinitionBuilder());
    builders.put("CKAN", new CkanDefinitionBuilder());
    builders.put("CSW", new CswDefinitionBuilder());
    builders.put("ARCGIS", new ArcgisDefinitionBuilder());
  }

  /**
   * Creates instance of the builder.
   *
   * @param engine engine
   */
  public MigrationSiteBuilder(Engine engine) {
    this.engine = engine;
  }
  
  public void buildSite(MigrationHarvestSite site) throws DataProcessorException {
    List<BrokerReference> brokers = listInputBrokers();
    
    AdaptorDefinitonBuilder builder = builders.get(site.type.toUpperCase());
    if (builder != null) {
      try {
        EntityDefinition brokerDefinition = builder.buildDefinition(site);
        BrokerReference ref = findReference(brokers, brokerDefinition);
        if (ref == null) {
          brokerDefinition.setLabel(site.title);
          BrokerReference brokerReference = engine.getBrokersService().createBroker(brokerDefinition, Locale.getDefault());
        }
      } catch (InvalidDefinitionException ex) {
        throw new DataProcessorException(String.format("Error importing site: %s", site.title), ex);
      }
    }
  }

  private List<BrokerReference> listInputBrokers() throws DataProcessorException {
    return engine.getBrokersService().getBrokersDefinitions(BrokerReference.Category.INBOUND, Locale.getDefault());
  }

  private BrokerReference findReference(List<BrokerReference> brokers, EntityDefinition brokerDefinition) {
    return brokers.stream().filter(br -> br.getBrokerDefinition().equals(brokerDefinition)).findFirst().orElse(null);
  }

  private static interface AdaptorDefinitonBuilder {

    EntityDefinition buildDefinition(MigrationHarvestSite site) throws InvalidDefinitionException;
  }

  private static class WafDefinitionBuilder implements AdaptorDefinitonBuilder {

    @Override
    public EntityDefinition buildDefinition(MigrationHarvestSite site) throws InvalidDefinitionException {
      try {
        EntityDefinition entityDefinition = new EntityDefinition();
        WafBrokerDefinitionAdaptor adaptor = new WafBrokerDefinitionAdaptor(entityDefinition);
        adaptor.setHostUrl(new URL(site.host));
        return entityDefinition;
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid site definition."), ex);
      }
    }
  }

  private static class CkanDefinitionBuilder implements AdaptorDefinitonBuilder {

    @Override
    public EntityDefinition buildDefinition(MigrationHarvestSite site) throws InvalidDefinitionException {
      try {
        EntityDefinition entityDefinition = new EntityDefinition();
        CkanBrokerDefinitionAdaptor adaptor = new CkanBrokerDefinitionAdaptor(entityDefinition);
        int apiPart = site.host.toLowerCase().indexOf("/api/3");
        String apiUrl = apiPart >= 0 ? site.host.substring(0, apiPart) : site.host;
        adaptor.setHostUrl(new URL(apiUrl));
        return entityDefinition;
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid site definition."), ex);
      }
    }
  }
  
  private static class ArcgisDefinitionBuilder  implements AdaptorDefinitonBuilder {

    @Override
    public EntityDefinition buildDefinition(MigrationHarvestSite site) throws InvalidDefinitionException {
      try {
        EntityDefinition entityDefinition = new EntityDefinition();
        AgsBrokerDefinitionAdaptor adaptor = new AgsBrokerDefinitionAdaptor(entityDefinition);
        int restIdx = site.host.toLowerCase().indexOf("/rest/services");
        String restUrl = restIdx>=0? site.host.substring(0, restIdx): site.host;
        adaptor.setHostUrl(new URL(restUrl));
        
        return entityDefinition;
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid site definition."), ex);
      }
    }
    
  }

  private static class CswDefinitionBuilder implements AdaptorDefinitonBuilder {

    @Override
    public EntityDefinition buildDefinition(MigrationHarvestSite site) throws InvalidDefinitionException {
      try {
        EntityDefinition entityDefinition = new EntityDefinition();
        CswBrokerDefinitionAdaptor adaptor = new CswBrokerDefinitionAdaptor(new ProfilesProvider().newProfiles(), entityDefinition);
        int queryIdx = site.host.indexOf("?");
        String cswUrl = queryIdx >= 0 ? site.host.substring(0, queryIdx) : site.host;
        adaptor.setHostUrl(new URL(cswUrl));
        
        String profileId = getProfileId(site.protocol);
        adaptor.setProfile(profiles.getProfileById(profileId));
        
        return entityDefinition;
      } catch (ParserConfigurationException|SAXException|XPathExpressionException|IOException ex) {
        throw new InvalidDefinitionException(String.format("Invalid site definition."), ex);
      }
    }

    private String getProfileId(String protocol) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(protocol)));
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      return (String)xPath.evaluate("/protocol[@type='CSW']/profile", doc, XPathConstants.STRING);
    }
  }
}
