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
package com.esri.geoportal.harvester.unc;

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.base.DataCollector;
import com.esri.geoportal.harvester.api.base.DataPrintStreamOutput;
import com.esri.geoportal.harvester.api.base.SimpleInitContext;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Unc application.
 * Java application designated to harvest UNC repositories and push data into the pipeline.
 * It takes a list of UNC paths to harvest as arguments.
 */
public class UncApplication {
  /**
   * Main method.
   * @param args arguments - list of files
   * @throws Exception if any exception occurs
   */
  public static void main(String[] args) throws Exception {
    DataPrintStreamOutput destination = new DataPrintStreamOutput(System.out);
    
    for (String sFile: args) {
      ArrayList<ProcessInstance.Listener> listeners = new ArrayList<>();
      UncConnector connector = new UncConnector();
      File start = new File(sFile);
      EntityDefinition def = new EntityDefinition();
      UncBrokerDefinitionAdaptor adaptor = new UncBrokerDefinitionAdaptor(def);
      adaptor.setRootFolder(start);
      
      InputBroker hv = connector.createBroker(def);
      hv.initialize(new SimpleInitContext(new Task(null,hv,null),listeners));
      DataCollector dataCollector = new DataCollector(hv, Arrays.asList(new DataPrintStreamOutput[]{destination}),listeners);
      dataCollector.collect();
    }
  }
}
