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
package com.esri.geoportal.harvester.ckan.data.gov;

import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.ckan.CkanBroker;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CKAN broker.
 */
/*package*/ class DataGovBroker extends CkanBroker {
  private static final Logger LOG = LoggerFactory.getLogger(DataGovBroker.class);
  
  private final DataGovConnector connector;
  private final DataGovBrokerDefinitionAdaptor definition;
  
  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   * @param metaBuilder meta builder
   */
  public DataGovBroker(DataGovConnector connector, DataGovBrokerDefinitionAdaptor definition, MetaBuilder metaBuilder) {
    super(connector, definition, metaBuilder);
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("DATA.GOV",definition.getHostUrl().toExternalForm(),null);
  }

  @Override
  public String toString() {
    return String.format("DATA.GOV [%s][%s]", definition.getHostUrl(), definition.getXmlUrl());
  }

  @Override
  public InputConnector getConnector() {
    return connector;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }
}
