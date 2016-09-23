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
package com.esri.geoportal.harvester.agpsrc;

import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.xml.SimpleDcMetaBuilder;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.base.DataCollector;
import com.esri.geoportal.harvester.api.base.DataPrintStreamOutput;
import com.esri.geoportal.harvester.api.base.SimpleInitContext;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Agp input application.
 */
public class AgpInputApplication {
  
  public static void main(String[] args) throws Exception {
    if (args.length==3) {
        DataPrintStreamOutput destination = new DataPrintStreamOutput(System.out);
        MetaBuilder metaBuilder = new SimpleDcMetaBuilder();
        AgpInputConnector connector = new AgpInputConnector(metaBuilder);
        URL start = new URL(args[0]);
        EntityDefinition def = new EntityDefinition();
        AgpInputBrokerDefinitionAdaptor adaptor = new AgpInputBrokerDefinitionAdaptor(def);
        adaptor.setHostUrl(start);
        adaptor.setCredentials(new SimpleCredentials(args[1], args[2]));
        
        InputBroker broker = null;
        try {
          ArrayList<ProcessInstance.Listener> listeners = new ArrayList<>();
          broker = connector.createBroker(def);
          broker.initialize(new SimpleInitContext(new Task(null, broker, null),listeners));
          DataCollector dataCollector = new DataCollector( broker, Arrays.asList(new DataPrintStreamOutput[]{destination}), listeners);
          dataCollector.collect();
        } finally {
          if (broker!=null) {
            broker.terminate();
          }
        }
      }
  }
}
