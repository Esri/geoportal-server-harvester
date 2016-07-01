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
/**
 * CSW input.
 * <p>
 * Catalogue Service for Web (CSW) is an OGC specification of the service allowing
 * to search and retrieve data through HTTP requests.
 * <p>
 * In order to define broker, an URL to the CSW end point is required with mandatory
 * profile id and optional user name and the password if resource is protected. 
 * With all these information, CSW input will query that end point and harvest
 * data available within.
 * <p>
 * Example of the JSON CSW definition is below:
 * <pre><code>
 
   {
     "type": "CSW",
     "properties" : {
       "csw-host-url": "http://gptogc.esri.com:8080/geoportal/csw",
       "csw-profile-id": "urn:ogc:CSW:2.0.2:HTTP:OGCCORE:ESRI:GPT",
       "cred-username": "admin",
       "cred-password": "admin"
     }
   }
 * </code></pre>
 * In order to use such definition to create broker it must be converted to the
 * instance of {@link com.esri.geoportal.harvester.api.defs.EntityDefinition}.
 * The easiest way to do it is to use Jackson ObjectMapper:
 * <pre><code>
 * 
   String JSON = ...;
   ObjectMapper mapper = new ObjectMapper();
   EntityDefinition definition = mapper.readValue(JSON,EntityDefinition.class);
   CswConnector connector = new CswConnector();
   InputBroker broker = connector.createBroker(definition);
 * </code></pre>
 * At this moment, broker object can be used to retrieve data through the 
 * {@link com.esri.geoportal.harvester.api.specs.InputBroker} interface.
 */
package com.esri.geoportal.harvester.csw;
