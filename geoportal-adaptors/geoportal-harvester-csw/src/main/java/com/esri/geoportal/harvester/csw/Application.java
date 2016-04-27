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
import com.esri.geoportal.commons.csw.client.ObjectFactory;
import com.esri.geoportal.commons.robots.BotsConfig;
import com.esri.geoportal.commons.robots.BotsMode;
import com.esri.geoportal.harvester.api.DataSource;
import com.esri.geoportal.harvester.api.support.DataCollector;
import com.esri.geoportal.harvester.api.support.DataPrintStreamDestination;
import java.net.URL;
import java.util.Arrays;

/**
 * Application.
 */
public class Application {
  public static void main(String[] args) throws Exception {
    if (args.length==2) {
      ObjectFactory of = new ObjectFactory();
      IProfiles profiles = of.newProfiles();
      IProfile profile = profiles.getProfileById(args[1]);
      DataPrintStreamDestination destination = new DataPrintStreamDestination(System.out);
      
      if (profile!=null) {
        URL start = new URL(args[0]);
        CswAttributesAdaptor initParams = new CswAttributesAdaptor();
        initParams.setHostUrl(start);
        initParams.setProfile(profile);
        initParams.setBotsConfig(BotsConfig.DEFAULT);
        initParams.setBotsMode(BotsMode.inherit);
        try (DataSource csw = new CswDataSource(initParams);) {
          DataCollector<String> dataCollector = new DataCollector<>(csw, Arrays.asList(new DataPrintStreamDestination[]{destination}));
          dataCollector.collect();
        }
      }
    }
  }
}
