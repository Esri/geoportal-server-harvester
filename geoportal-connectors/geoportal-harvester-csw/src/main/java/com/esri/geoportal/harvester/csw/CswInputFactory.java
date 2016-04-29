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
import com.esri.geoportal.harvester.api.DataConnector;
import com.esri.geoportal.harvester.api.DataBrokerUiTemplate;
import com.esri.geoportal.harvester.api.DataBrokerUiTemplate.Choice;
import static com.esri.geoportal.harvester.csw.CswAttributesAdaptor.P_HOST_URL;
import static com.esri.geoportal.harvester.csw.CswAttributesAdaptor.P_PROFILE_ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.esri.geoportal.harvester.api.DataInput;
import com.esri.geoportal.harvester.api.DataInputFactory;

/**
 * Folder input factory.
 */
public class CswInputFactory implements DataInputFactory {

  @Override
  public DataInput create(DataConnector connector) throws IllegalArgumentException {
    CswAttributesAdaptor attr = new CswAttributesAdaptor(connector.getAttributes());
    return new CswDataInput(attr);
  }

  @Override
  public DataBrokerUiTemplate getTemplate() {
    List<DataBrokerUiTemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new DataBrokerUiTemplate.StringArgument(P_HOST_URL, "URL"));
    ObjectFactory of = new ObjectFactory();
    IProfiles profiles = of.newProfiles();
    Choice<String>[] choices = profiles.listAll().stream().map(p->new Choice<String>(p.getId(),p.getName())).toArray(Choice[]::new);
    arguments.add(new DataBrokerUiTemplate.ChoiceArgument(P_PROFILE_ID, "Profile", Arrays.asList(choices)){
      public String getDefault() {
        IProfile defaultProfile = profiles.getDefaultProfile();
        return defaultProfile!=null? defaultProfile.getId(): null;
      }
    });
    return new DataBrokerUiTemplate("CSW", "Catalogue service for the web", arguments);
  }
  
}
