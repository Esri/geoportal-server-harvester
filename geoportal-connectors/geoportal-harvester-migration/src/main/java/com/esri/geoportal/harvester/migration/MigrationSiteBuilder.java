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

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.services.Engine;
import com.esri.geoportal.harvester.engine.utils.BrokerReference;
import com.esri.geoportal.harvester.waf.WafBrokerDefinitionAdaptor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Migration harvest site builder.
 */
/*package*/ class MigrationSiteBuilder {
  private final Engine engine;
  
  private final Map<String,WafDefinitionBuilder> builders = new HashMap<>();
  {
    builders.put("WAF", new WafDefinitionBuilder());
  }

  /**
   * Creates instance of the builder.
   * @param engine engine
   */
  public MigrationSiteBuilder(Engine engine) {
    this.engine = engine;
  }
  
  public void buildSite(MigrationHarvestSite site) throws DataProcessorException {
    WafDefinitionBuilder builder = builders.get(site.type.toUpperCase());
    if (builder!=null) {
      try {
        EntityDefinition brokerDefinition = builder.buildDefinition(site);
        brokerDefinition.setLabel(site.title);
        BrokerReference brokerReference = engine.getBrokersService().createBroker(brokerDefinition, Locale.getDefault());
      } catch (InvalidDefinitionException ex) {
        throw new DataProcessorException(String.format("Error importing site: %s", site.title), ex);
      }
    }
  }
  
  private List<BrokerReference> listInputBrokers() throws DataProcessorException {
    return engine.getBrokersService().getBrokersDefinitions(BrokerReference.Category.INBOUND, Locale.getDefault());
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
}
