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
package com.esri.geoportal.harvester.beans;

import com.esri.geoportal.harvester.engine.impl.DefaultBrokersService;
import com.esri.geoportal.harvester.engine.managers.BrokerDefinitionManager;
import com.esri.geoportal.harvester.engine.managers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.managers.OutboundConnectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Brokers service bean.
 */
@Service
public class BrokersServiceBean extends DefaultBrokersService {

  /**
   * Creates instance of the bean.
   * @param inboundConnectorRegistry inbound connector registry.
   * @param outboundConnectorRegistry outbound connector registry
   * @param brokerDefinitionManager  broker definition manager
   */
  @Autowired
  public BrokersServiceBean(InboundConnectorRegistry inboundConnectorRegistry, OutboundConnectorRegistry outboundConnectorRegistry, BrokerDefinitionManager brokerDefinitionManager) {
    super(inboundConnectorRegistry, outboundConnectorRegistry, brokerDefinitionManager);
  }
  
}
