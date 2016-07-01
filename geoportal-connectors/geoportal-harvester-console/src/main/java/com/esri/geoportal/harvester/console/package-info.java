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
 * Console output.
 * <p>
 * Allows to publish information into the console.
 * <p>
 * Example of the JSON FOLDER definition is below:
 * <pre><code>
 
   {
     "type": "CONSOLE"
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
   ConsoleConnector connector = new ConsoleConnector();
   InputBroker broker = connector.createBroker(definition);
 * </code></pre>
 * At this moment, broker object can be used to publish data through the 
 * {@link com.esri.geoportal.harvester.api.specs.OutputBroker} interface.
 */
package com.esri.geoportal.harvester.console;
