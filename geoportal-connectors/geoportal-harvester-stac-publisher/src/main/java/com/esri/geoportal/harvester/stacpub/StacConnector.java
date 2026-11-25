/*
 * Copyright 2020 Esri, Inc.
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
package com.esri.geoportal.harvester.stacpub;

import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_PASSWORD;
import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_USERNAME;
import static com.esri.geoportal.harvester.stacpub.StacConstants.*;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * STAC connector.
 */
public class StacConnector implements OutputConnector<OutputBroker> {

  public static final String TYPE = "STAC-OUT";
    
  /**
   * Creates instance of the connector.
   */
  public StacConnector() {
    }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("StacPubResource", locale);
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("stac.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("stac.hint");
      }
    });
    arguments.add(new UITemplate.StringArgument(P_CRED_USERNAME, bundle.getString("stac.username"), true));
    arguments.add(new UITemplate.StringArgument(P_CRED_PASSWORD, bundle.getString("stac.password"), true) {
      public boolean isPassword() {
        return true;
      }
    });
    arguments.add(new UITemplate.StringArgument(P_COLLECTION_ID, bundle.getString("stac.collectionId"), true));
        

    return new UITemplate(getType(), bundle.getString("stac"), arguments);
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
      StacBrokerDefinitionAdaptor stacBrokerDefinitionAdaptor = new StacBrokerDefinitionAdaptor(definition);
  }

  @Override
  public OutputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new StacOutputBroker(this, new StacBrokerDefinitionAdaptor(definition));
  }

  @Override
  public String getResourceLocator(EntityDefinition definition) {
    try {
      StacBrokerDefinitionAdaptor adaptor = new StacBrokerDefinitionAdaptor(definition);
      return adaptor.getHostUrl()!=null? adaptor.getHostUrl().toExternalForm(): "";
    } catch (InvalidDefinitionException ex) {
      return "";
    }
  }
}
