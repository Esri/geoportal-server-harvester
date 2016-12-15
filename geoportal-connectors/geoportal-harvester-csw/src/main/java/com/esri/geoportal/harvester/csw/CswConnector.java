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
package com.esri.geoportal.harvester.csw;

import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_PASSWORD;
import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_USERNAME;
import com.esri.geoportal.commons.csw.client.IProfile;
import com.esri.geoportal.commons.csw.client.IProfiles;
import com.esri.geoportal.commons.csw.client.impl.ProfilesProvider;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.defs.UITemplate.Choice;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import static com.esri.geoportal.harvester.csw.CswBrokerDefinitionAdaptor.P_HOST_URL;
import static com.esri.geoportal.harvester.csw.CswBrokerDefinitionAdaptor.P_PROFILE_ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * CSW connector.
 * @see com.esri.geoportal.harvester.csw API
 */
public class CswConnector implements InputConnector<InputBroker> {
  public static final String TYPE = "CSW";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("CswResource", locale);
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("csw.url"), true));
    arguments.add(new UITemplate.StringArgument(P_CRED_USERNAME, bundle.getString("csw.username"), false));
    arguments.add(new UITemplate.StringArgument(P_CRED_PASSWORD, bundle.getString("csw.password"), false) {
      public boolean isPassword() {
        return true;
      }
    });
    ProfilesProvider of = new ProfilesProvider();
    IProfiles profiles = of.newProfiles();
    Choice<String>[] choices = profiles.listAll().stream().map(p->new Choice<String>(p.getId(),p.getName())).toArray(Choice[]::new);
    arguments.add(new UITemplate.ChoiceArgument(P_PROFILE_ID, bundle.getString("csw.profile"), Arrays.asList(choices)){
      public String getDefault() {
        IProfile defaultProfile = profiles.getDefaultProfile();
        return defaultProfile!=null? defaultProfile.getId(): null;
      }
    });
    return new UITemplate(getType(), bundle.getString("csw"), arguments);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new CswBroker(this, new CswBrokerDefinitionAdaptor(definition));
  }
}
