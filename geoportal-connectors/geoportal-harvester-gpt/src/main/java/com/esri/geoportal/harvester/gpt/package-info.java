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
 * Geoportal Server 2.0 catalog output.
 * <p>
 * Allows to publish information into the Geoportal 2.0 catalog.
 * <p>
 * In order to define broker, an URL to the instance of Geoportal Catalog 2.0 is 
 * required together with mandatory credentials. Broker will publish metadata to
 * it, where new metadata will simply be created while metadata already existing 
 * will be just updated.
 * <p>
 * Example of the JSON GPT definition is below:
 * <pre><code>
 
   {
     "type": "GPT",
     "properties" : {
        "gpt-host-url": "http://localhost:8080/geoportal",
        "cred-username": "gptadmin",
        "cred-password": "gptadmin"
     }
   }
 * </code></pre>
 * Optionally, property "gpt-force-add" can be used; if set to "true" than all
 * metadata will be created and no updates will occur. This option may be useful 
 * for initial catalog harvest since it significantly improves performance.
 * <p>
 * In order to use such definition to create broker it must be converted to the
 * instance of {@link com.esri.geoportal.harvester.api.defs.EntityDefinition}.
 * The easiest way to do it is to use Jackson ObjectMapper:
 * <pre><code>
 * 
   String JSON = ...;
   ObjectMapper mapper = new ObjectMapper();
   EntityDefinition definition = mapper.readValue(JSON,EntityDefinition.class);
   GptConnector connector = new GptConnector();
   InputBroker broker = connector.createBroker(definition);
 * </code></pre>
 * At this moment, broker object can be used to publish data through the 
 * {@link com.esri.geoportal.harvester.api.specs.OutputBroker} interface.
 */
package com.esri.geoportal.harvester.gpt;
