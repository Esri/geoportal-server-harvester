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

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.engine.support.BrokerInfo;
import com.esri.geoportal.harvester.beans.EngineBean;
import com.esri.geoportal.harvester.engine.support.BrokerInfo.Category;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Broker controller.
 * <p>
 * Provides access to brokers.
 * <pre><code>
 
   GET /rest/harvester/brokers?category=INBOUND   - lists all inbound brokers (inputs)
   GET /rest/harvester/brokers?category=OUTBOUND  - lists all outbound brokers (outputs)
   GET /rest/harvester/brokers/{brokerId}         - gets broker by id (input or output)
   
   DELETE /rest/harvester/brokers/{brokerId}      - deletes broker by id (input or output)
   PUT /rest/harvester/brokers                    - creates new broker (input or output; body of the request defines broker)
   POST /rest/harvester/brokers/{brokerId}        - updates existing broker by id (input or output; body of the request defines broker)
 * </code></pre>
 * Each GET request returns JSON with broker definition. It is an array of definitions when
 * listing and a single definition when getting by id:
 * <pre><code>
   [
    {
      "uuid":"761e848d-37fc-4540-b707-298edb923156",
      "category":"INBOUND",
      "brokerDefinition":{
        "type":"WAF",
        "label":"My WAF",
        "properties":{
          "waf-host-url":"http://my.waf.com",
          "cred-username":"",
          "cred-password":""
        }
      }
    }
   ]
 * </code></pre>
 * Both PUT and POST require body of a single definition, similar to the one above.
 */
@RestController
public class BrokerController {
  private static final Logger LOG = LoggerFactory.getLogger(BrokerController.class);
    
  @Autowired
  private EngineBean engine;
  

  /**
   * Lists all input brokers.
   * @param category category (optional)
   * @return array of broker infos
   */
  @RequestMapping(value = "/rest/harvester/brokers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BrokerInfo[]> listBrokers(@RequestParam(value = "category", required = false) String category) {
    try {
      LOG.debug(String.format("GET /rest/harvester/brokers%s",category!=null? "&category="+category: ""));
      Category ctg = null;
      try {
        ctg = Category.parse(category);
      } catch (IllegalArgumentException ex) {
        // ignore
      }
      return new ResponseEntity<>(engine.getBrokersDefinitions(ctg).toArray(new BrokerInfo[0]), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error listing all brokers"), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Get a single broker.
   * @param brokerId broker id
   * @return broker info or <code>null</code> if no broker found
   */
  @RequestMapping(value = "/rest/harvester/brokers/{brokerId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BrokerInfo> getBroker(@PathVariable UUID brokerId) {
    try {
      LOG.debug(String.format("GET /rest/harvester/brokers/%s", brokerId));
      return new ResponseEntity<>(engine.findBroker(brokerId), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error getting broker: %s", brokerId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Deletes a broker.
   * @param brokerId broker id
   * @return broker info
   */
  @RequestMapping(value = "/rest/harvester/brokers/{brokerId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BrokerInfo> deleteBroker(@PathVariable UUID brokerId) {
    try {
      LOG.debug(String.format("DELETE /rest/harvester/brokers/%s", brokerId));
      BrokerInfo brokerInfo = engine.findBroker(brokerId);
      if (brokerInfo!=null) {
        engine.deleteBroker(brokerId);
      }
      return new ResponseEntity<>(brokerInfo, HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error deleting broker: %s", brokerId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Adds a new task.
   * @param brokerDefinition broker definition
   * @return broker info of the newly created broker
   */
  @RequestMapping(value = "/rest/harvester/brokers", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BrokerInfo> createBroker(@RequestBody EntityDefinition brokerDefinition) {
    try {
      LOG.debug(String.format("PUT /rest/harvester/brokers <-- %s", brokerDefinition));
      return new ResponseEntity<>(engine.createBroker(brokerDefinition), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error creating broker: %s", brokerDefinition), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Adds a new task.
   * @param brokerDefinition broker definition
   * @param brokerId broker id
   * @return broker info of the task which has been replaced
   */
  @RequestMapping(value = "/rest/harvester/brokers/{brokerId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BrokerInfo> updateBroker(@RequestBody EntityDefinition brokerDefinition, @PathVariable UUID brokerId) {
    try {
      LOG.debug(String.format("POST /rest/harvester/brokers/%s <-- %s", brokerId, brokerDefinition));
      return new ResponseEntity<>(engine.updateBroker(brokerId, brokerDefinition), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error updating broker: %s <-- %s", brokerId, brokerDefinition), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
}
