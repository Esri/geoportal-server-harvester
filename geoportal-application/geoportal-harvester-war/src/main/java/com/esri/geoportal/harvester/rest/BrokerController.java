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
package com.esri.geoportal.harvester.rest;

import com.esri.geoportal.harvester.engine.BrokerInfo;
import com.esri.geoportal.harvester.api.BrokerDefinition;
import com.esri.geoportal.harvester.beans.EngineBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Broker controller
 */
@RestController
public class BrokerController {
    
  @Autowired
  private EngineBean engine;
  
  /**
   * Lists all inbound connectors. A connector might be: WAF, CSW, etc.
   * @return array of connector templates
   */
  @RequestMapping(value = "/rest/harvester/brokers/inbound", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public BrokerInfo[] listInboundBrokers() {
    return engine.getInboundBrokersDefinitions().toArray(new BrokerInfo[0]);
  }
  
  /**
   * Lists all outbound connectors. A connector might be: GPT, FOLDER, etc.
   * @return array of connector templates
   */
  @RequestMapping(value = "/rest/harvester/brokers/outbound", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public BrokerInfo[] listOutboundBrokers() {
    return engine.getOutboundBrokersDefinitions().toArray(new BrokerInfo[0]);
  }

}
