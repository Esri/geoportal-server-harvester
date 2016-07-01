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
 * WAF input.
 * <p>
 * Web Accessible Folder (WAF) is a loose term to describe a network resource
 * offered as an HTML page (or pages) with series of links in it leading to
 * some useful, downloadable information. In the context of harvesting, all
 * such links are pointing to the metadata files. Typically, such repository is
 * organized in a directory fashion.
 * <p>
 * In order to define broker, an URL to the resource is required with optional
 * user name and the password if resource is protected. With all these information,
 * WAF input will crawl that end point and follow links within in search for more
 * metadata files. All the links being followed are sub-links of the initial URL,
 * i.e. broker will not go beyond the server or even any level up within the directory.
 * <p>
 * Example of the JSON WAF definition is below:
 * <pre><code>
 
   {
     "type": "WAF",
     "properties" : {
       "waf-host-url": "http://localhost:8080/waf/metadata/iso",
       "cred-username": "dowjones",
       "cred-password": "secret"
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
   WafConnector connector = new WafConnector();
   InputBroker broker = connector.createBroker(definition);
 * </code></pre>
 * At this moment, broker object can be used to retrieve data through the 
 * {@link com.esri.geoportal.harvester.api.specs.InputBroker} interface.
 */
package com.esri.geoportal.harvester.waf;
