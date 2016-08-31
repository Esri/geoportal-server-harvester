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
package com.esri.geoportal.harvester.ags;

import com.esri.geoportal.harvester.api.base.DataCollector;
import com.esri.geoportal.harvester.api.base.DataPrintStreamOutput;
import com.esri.geoportal.commons.meta.xml.SimpleDcMetaHandler;
import com.esri.geoportal.harvester.api.base.SimpleInitContext;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import java.net.URL;
import java.util.Arrays;

/**
 * AgsApplication.
 */
public class AgsApplication {
  public static void main(String[] args) throws Exception {
    DataPrintStreamOutput destination = new DataPrintStreamOutput(System.out);
    
    for (String sUrl: args) {
      SimpleDcMetaHandler metaHandler = new SimpleDcMetaHandler();
      AgsConnector connector = new AgsConnector(metaHandler);
      URL start = new URL(sUrl);
      EntityDefinition def = new EntityDefinition();
      AgsBrokerDefinitionAdaptor adaptor = new AgsBrokerDefinitionAdaptor(def);
      adaptor.setHostUrl(start);
      InputBroker hv = connector.createBroker(def);
      hv.initialize(new SimpleInitContext(new Task(null,hv,null)));
      DataCollector dataCollector = new DataCollector(hv, Arrays.asList(new DataPrintStreamOutput[]{destination}));
      dataCollector.collect();
    }
  }
}
