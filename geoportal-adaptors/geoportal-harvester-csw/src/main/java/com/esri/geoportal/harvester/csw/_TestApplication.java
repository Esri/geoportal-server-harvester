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
import com.esri.geoportal.harvester.api.DataAdaptorDefinition;
import com.esri.geoportal.harvester.api.DataDestination;
import com.esri.geoportal.harvester.api.DataDestinationException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.DataSource;
import com.esri.geoportal.harvester.api.support.DataCollector;
import java.net.URL;
import java.util.Arrays;

/**
 * Test application.
 */
public class _TestApplication {

  public static void main(String[] args) throws Exception {
    String sUrl = "http://gptogc.esri.com/geoportal/csw?request=GetCapabilities&service=CSW";
    String sProfile = "urn:ogc:CSW:2.0.2:HTTP:OGCCORE:ESRI:GPT";
    DataDestination<String> dst = new DataDestination<String>() {
      int counter = 0;
      
      @Override
      public void publish(DataReference<String> ref) throws DataDestinationException {
        counter++;
        if (counter%20==0) {
          System.out.println(String.format("Counter: %d", counter));
        }
      }

      @Override
      public DataAdaptorDefinition getDefinition() {
        return null;
      }

      @Override
      public String getDescription() {
        return "adhoc";
      }

      @Override
      public void close() throws Exception {
      }
    };

    ObjectFactory of = new ObjectFactory();
    IProfiles profiles = of.newProfiles();
    IProfile profile = profiles.getProfileById(sProfile);

    if (profile != null) {
      URL start = new URL(sUrl);
      CswAttributesAdaptor initParams = new CswAttributesAdaptor();
      initParams.setHostUrl(start);
      initParams.setProfile(profile);
      initParams.setBotsConfig(BotsConfig.DEFAULT);
      initParams.setBotsMode(BotsMode.inherit);
      try (DataSource csw = new CswDataSource(initParams);) {
        DataCollector<String> dataCollector = new DataCollector<>(csw, Arrays.asList(new DataDestination[]{dst}));
        dataCollector.collect();
      }
    }
  }
}
