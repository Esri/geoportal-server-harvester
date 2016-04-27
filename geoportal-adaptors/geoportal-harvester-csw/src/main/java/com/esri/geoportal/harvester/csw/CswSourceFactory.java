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

import com.esri.geoportal.commons.csw.client.IProfiles;
import com.esri.geoportal.commons.csw.client.ObjectFactory;
import com.esri.geoportal.harvester.api.DataAdaptorTemplate;
import com.esri.geoportal.harvester.api.DataAdaptorTemplate.Choice;
import com.esri.geoportal.harvester.api.DataSource;
import com.esri.geoportal.harvester.api.DataSourceFactory;
import static com.esri.geoportal.harvester.csw.CswAttributesAdaptor.P_HOST_URL;
import static com.esri.geoportal.harvester.csw.CswAttributesAdaptor.P_PROFILE_ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Folder publisher factory.
 */
public class CswSourceFactory implements DataSourceFactory {

  @Override
  public DataSource create(Map<String, String> attributes) throws IllegalArgumentException {
    CswAttributesAdaptor attr = new CswAttributesAdaptor(attributes);
    return new CswDataSource(attr);
  }

  @Override
  public DataAdaptorTemplate getTemplate() {
    List<DataAdaptorTemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new DataAdaptorTemplate.StringArgument(P_HOST_URL, "URL"));
    ObjectFactory of = new ObjectFactory();
    IProfiles profiles = of.newProfiles();
    Choice<String>[] choices = profiles.listAll().stream().map(p->new Choice<String>(p.getId(),p.getName())).toArray(Choice[]::new);
    arguments.add(new DataAdaptorTemplate.ChoiceArgument(P_PROFILE_ID, "Profile", Arrays.asList(choices)){
      public String getDefault() {
        return choices[0].getName();
      }
    });
    return new DataAdaptorTemplate("CSW", "Catalogue service for the web", arguments);
  }
  
}
