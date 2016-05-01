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
import com.esri.geoportal.harvester.api.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.n.BrokerDefinition;
import com.esri.geoportal.harvester.api.support.DataCollector;
import java.net.URL;
import java.util.Arrays;
import com.esri.geoportal.harvester.api.n.InputBroker;
import com.esri.geoportal.harvester.api.n.OutputBroker;
import java.io.IOException;

/**
 * Test application.
 */
public class _TestApplication {
  public static void main(String[] args) throws Exception {
    String sUrl = "http://data.nodc.noaa.gov/nodc/archive/metadata/test/granule/iso/ghrsst_new";
    OutputBroker<String> dst = new OutputBroker<String>() {
      int counter = 0;
      
      @Override
      public void publish(DataReference<String> ref) throws DataOutputException {
        counter++;
        if (counter%20==0) {
          System.out.println(String.format("Counter: %d", counter));
        }
      }

      @Override
      public void close() throws IOException {
        // no closing necessary
      }
    };
    
      WafConnector connector = new WafConnector();
      URL start = new URL(sUrl);
      WafDefinition arguments = new WafDefinition();
      arguments.setHostUrl(start);
      arguments.setBotsConfig(BotsConfig.DEFAULT);
      arguments.setBotsMode(BotsMode.never);
      try (InputBroker<String> hv = connector.createBroker(arguments)) {
        DataCollector<String> dataCollector = new DataCollector<>(hv, Arrays.asList(new OutputBroker[]{dst}));
        dataCollector.collect();
      }
  }
}
