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
package com.esri.geoportal.harvester.csw;

import com.esri.geoportal.commons.csw.client.IProfile;
import com.esri.geoportal.commons.csw.client.IProfiles;
import com.esri.geoportal.commons.csw.client.impl.ProfilesProvider;
import com.esri.geoportal.commons.robots.BotsConfig;
import com.esri.geoportal.commons.robots.BotsMode;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.base.DataCollector;
import com.esri.geoportal.harvester.api.base.DataPrintStreamOutput;
import com.esri.geoportal.harvester.api.base.SimpleInitContext;
import com.esri.geoportal.harvester.api.defs.Task;
import java.net.URL;
import java.util.Arrays;
import com.esri.geoportal.harvester.api.specs.InputBroker;

/**
 * CSW application.
 * Java application designated to harvest CSW repositories and push data into the pipeline.
 * It takes GetCapabilities URL and a profile id as arguments.
 */
public class CswApplication {
  /**
   * Man method
   * @param args arguments: GetCapabilities URL and a profile id
   * @throws Exception if any exception occurs
   */
  public static void main(String[] args) throws Exception {
    if (args.length==2) {
      ProfilesProvider of = new ProfilesProvider();
      IProfiles profiles = of.newProfiles();
      IProfile profile = profiles.getProfileById(args[1]);
      DataPrintStreamOutput destination = new DataPrintStreamOutput(System.out);
      
      if (profile!=null) {
        CswConnector connector = new CswConnector();
        URL start = new URL(args[0]);
        EntityDefinition def = new EntityDefinition();
        CswBrokerDefinitionAdaptor adaptor = new CswBrokerDefinitionAdaptor(def);
        adaptor.setHostUrl(start);
        adaptor.setProfile(profile);
        adaptor.setBotsConfig(BotsConfig.DEFAULT);
        adaptor.setBotsMode(BotsMode.inherit);
        
        InputBroker csw = null;
        try {
          csw = connector.createBroker(def);
          csw.initialize(new SimpleInitContext(new Task(null, csw, null)));
          DataCollector dataCollector = new DataCollector(csw, Arrays.asList(new DataPrintStreamOutput[]{destination}));
          dataCollector.collect();
        } finally {
          if (csw!=null) {
            csw.terminate();
          }
        }
      }
    }
  }
}
