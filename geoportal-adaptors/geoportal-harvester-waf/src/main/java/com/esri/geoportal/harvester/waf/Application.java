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
import com.esri.geoportal.harvester.api.DataSource;
import com.esri.geoportal.harvester.impl.DataCollector;
import com.esri.geoportal.harvester.impl.DataPrintStreamDestination;
import java.net.URL;
import java.util.Arrays;

/**
 * Application.
 */
public class Application {
  public static void main(String[] args) throws Exception {
    DataPrintStreamDestination destination = new DataPrintStreamDestination(System.out);
    
    for (String sUrl: args) {
      URL start = new URL(sUrl);
      WafAttributesAdaptor attributes = new WafAttributesAdaptor();
      attributes.setHostUrl(start);
      attributes.setBotsConfig(BotsConfig.DEFAULT);
      attributes.setBotsMode(BotsMode.inherit);
      try (DataSource hv = new WafDataSource(attributes);) {
        DataCollector<String> dataCollector = new DataCollector<>(hv, Arrays.asList(new DataPrintStreamDestination[]{destination}));
        dataCollector.collect();
      }
    }
  }
}
