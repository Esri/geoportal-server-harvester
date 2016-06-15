/*
 * Copyright 2016 Esri, Inc..
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
package com.esri.geoportal.harvester.waf;

import com.esri.geoportal.commons.robots.BotsConfig;
import com.esri.geoportal.commons.robots.BotsMode;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import java.net.URL;
import java.util.Arrays;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.base.DataCollector;
import com.esri.geoportal.harvester.api.base.DataPrintStreamOutput;

/**
 * Waf application.
 * Java application designated to harvest WAF repositories and push data into the pipeline.
 * It takes a list of WAF URL's to harvest as arguments.
 */
public class WafApplication {
  /**
   * Main method.
   * @param args arguments - list of URL's
   * @throws Exception if any exception occurs
   */
  public static void main(String[] args) throws Exception {
    DataPrintStreamOutput destination = new DataPrintStreamOutput(System.out);
    
    for (String sUrl: args) {
      WafConnector connector = new WafConnector();
      URL start = new URL(sUrl);
      EntityDefinition def = new EntityDefinition();
      WafBrokerDefinitionAdaptor adaptor = new WafBrokerDefinitionAdaptor(def);
      adaptor.setHostUrl(start);
      adaptor.setBotsConfig(BotsConfig.DEFAULT);
      adaptor.setBotsMode(BotsMode.inherit);
      try (InputBroker hv = connector.createBroker(def)) {
        DataCollector dataCollector = new DataCollector(hv, Arrays.asList(new DataPrintStreamOutput[]{destination}));
        dataCollector.collect();
      }
    }
  }
}
