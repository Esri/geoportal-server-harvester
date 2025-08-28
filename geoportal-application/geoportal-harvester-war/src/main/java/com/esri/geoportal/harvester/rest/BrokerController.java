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
import com.esri.geoportal.harvester.engine.services.Engine;
import com.esri.geoportal.harvester.engine.utils.BrokerReference.Category;
import com.esri.geoportal.harvester.support.BrokerResponse;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import com.esri.geoportal.harvester.engine.utils.BrokerReference;
import org.owasp.esapi.ESAPI;

/**
 * Broker controller.
 * <p>
 * Provides access to brokers.
 * <pre><code>
 
   GET /rest/harvester/brokers?category=INBOUND   - lists all inbound brokers (inputs)
   GET /rest/harvester/brokers?category=OUTBOUND  - lists all outbound brokers (outputs)
   GET /rest/harvester/brokers/{brokerId}         - gets broker by id (input or output)
   
   DELETE /rest/harvester/brokers/{brokerId}      - deletes broker by id (input or output)
   POST /rest/harvester/brokers                   - creates new broker (input or output; body of the request defines broker)
   PUT /rest/harvester/brokers/{brokerId}         - updates existing broker by id (input or output; body of the request defines broker)
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
  private Engine engine;
  

  /**
   * Lists all input brokers.
   * @param category category (optional)
   * @return array of broker infos
   */
  @RequestMapping(value = "/rest/harvester/brokers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BrokerResponse[]> listBrokers(@RequestParam(value = "category", required = false) String category) {
    try {
      LOG.debug(formatForLog("GET /rest/harvester/brokers%s",category!=null? "&category="+category: ""));
      Category ctg = null;
      try {
        ctg = Category.parse(category);
      } catch (IllegalArgumentException ex) {
        // ignore
      }
      return new ResponseEntity<>(engine.getBrokersService().getBrokersDefinitions(ctg, LocaleContextHolder.getLocale()).stream().map(d->BrokerResponse.createFrom(d)).collect(Collectors.toList()).toArray(new BrokerResponse[0]), HttpStatus.OK);
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
  public ResponseEntity<BrokerResponse> getBroker(@PathVariable UUID brokerId) {
    try {
      LOG.debug(formatForLog("GET /rest/harvester/brokers/%s", brokerId));
      return new ResponseEntity<>(BrokerResponse.createFrom(engine.getBrokersService().findBroker(brokerId, LocaleContextHolder.getLocale())), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error getting broker: %s", brokerId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Deletes a broker.
   * @param brokerId broker id
   * @return broker info
   */
  @RequestMapping(value = "/rest/harvester/brokers/{brokerId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BrokerResponse> deleteBroker(@PathVariable UUID brokerId) {
    try {
      LOG.debug(formatForLog("DELETE /rest/harvester/brokers/%s", brokerId));
      engine.getBrokersService().deleteBroker(brokerId);
      return new ResponseEntity<>(BrokerResponse.createFrom(engine.getBrokersService().findBroker(brokerId, LocaleContextHolder.getLocale())), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error deleting broker: %s", brokerId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Adds a new task.
   * @param brokerDefinition broker definition
   * @return broker info of the newly created broker
   */
  @RequestMapping(value = "/rest/harvester/brokers", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BrokerResponse> createBroker(@RequestBody EntityDefinition brokerDefinition) {
    try {
      LOG.debug(ESAPI.encoder().encodeForHTML(String.format("POST /rest/harvester/brokers <-- %s", brokerDefinition)));
      return new ResponseEntity<>(BrokerResponse.createFrom(engine.getBrokersService().createBroker(brokerDefinition, LocaleContextHolder.getLocale())), HttpStatus.OK);
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
  @RequestMapping(value = "/rest/harvester/brokers/{brokerId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BrokerResponse> updateBroker(@RequestBody EntityDefinition brokerDefinition, @PathVariable UUID brokerId) {
    try {
      LOG.debug(formatForLog("PUT /rest/harvester/brokers/%s <-- %s", brokerId, brokerDefinition));
      BrokerReference brokerReference = engine.getBrokersService().updateBroker(brokerId, brokerDefinition, LocaleContextHolder.getLocale());
      engine.getTasksService().updateTaskDefinitions(brokerDefinition);
      return new ResponseEntity<>(BrokerResponse.createFrom(brokerReference), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error updating broker: %s <-- %s", brokerId, brokerDefinition), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
}
