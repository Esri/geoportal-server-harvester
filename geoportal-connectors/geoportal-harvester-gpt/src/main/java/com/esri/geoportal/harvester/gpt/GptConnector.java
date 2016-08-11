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
package com.esri.geoportal.harvester.gpt;

import com.esri.geoportal.commons.gpt.client.Client;
import com.esri.geoportal.harvester.api.base.CredentialsDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import static com.esri.geoportal.harvester.gpt.GptBrokerDefinitionAdaptor.P_CLEANUP;
import static com.esri.geoportal.harvester.gpt.GptBrokerDefinitionAdaptor.P_HOST_URL;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * GPT connector.
 * @see com.esri.geoportal.harvester.gpt API
 */
public class GptConnector implements OutputConnector<OutputBroker> {
  public static final String TYPE = "GPT";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate() {
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.StringArgument(P_HOST_URL, "URL", true));
    arguments.add(new UITemplate.StringArgument(CredentialsDefinitionAdaptor.P_CRED_USERNAME, "User name", true));
    arguments.add(new UITemplate.StringArgument(CredentialsDefinitionAdaptor.P_CRED_PASSWORD, "User password", true) {
      public boolean isPassword() {
        return true;
      }
    });
    arguments.add(new UITemplate.BooleanArgument(P_CLEANUP, "Perform cleanup"));
    return new UITemplate(getType(), "Geoportal Server 2.0", arguments);
  }

  @Override
  public OutputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    GptBrokerDefinitionAdaptor adaptor = new GptBrokerDefinitionAdaptor(definition);
    try {
      URL url = new URL(adaptor.getHostUrl().toExternalForm().replaceAll("/$", "")+"/");
      Client client = new Client(url, adaptor.getCredentials());
      return new GptBroker(this, adaptor, client);
    } catch (MalformedURLException ex) {
      throw new InvalidDefinitionException(String.format("Invalid url", adaptor.getHostUrl()), ex);
    }
  }
  
}
