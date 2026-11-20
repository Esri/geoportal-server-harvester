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
package com.esri.geoportal.harvester.stacpub;

import com.esri.geoportal.geoportal.commons.stac.client.Client;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.io.IOException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * STAC output broker.
 */
/*package*/ class StacOutputBroker implements OutputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(StacOutputBroker.class);

  private final StacConnector connector;
  private final StacBrokerDefinitionAdaptor definition;
  private Client client;
  private CloseableHttpClient httpClient;

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition
   */
  public StacOutputBroker(StacConnector connector, StacBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public PublishingStatus publish(DataReference ref) throws DataOutputException {

    return null;
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
    definition.override(context.getParams());
    this.httpClient = HttpClientBuilder.create().useSystemProperties().setRedirectStrategy(LaxRedirectStrategy.INSTANCE).build();


  }

  @Override
  public void terminate() {
    try {
      if (client!=null) {
        client.close();
      }
    } catch ( IOException ex) {
      LOG.error(String.format("Error terminating broker."), ex);
    }
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    // return definition.getCredentials() == null || definition.getCredentials().isEmpty() ? true : definition.getCredentials().equals(creds);
    return true;
  }

}
