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
package com.esri.geoportal.harvester.oai.pmh;

import static com.esri.geoportal.harvester.oai.pmh.OaiConstants.*;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * OAI-PMH connector.
 */
public class OaiConnector implements InputConnector<InputBroker> {

  public static final String TYPE = "OAI-PMH";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("OaiResource", locale);
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("oai.url"), true));
    args.add(new UITemplate.StringArgument(P_PREFIX, bundle.getString("oai.prefix"), true){
      @Override
      public String getHint() {
        return bundle.getString("oai.prefix.hint");
      }
    });
    args.add(new UITemplate.StringArgument(P_SET, bundle.getString("oai.set"), false));
    return new UITemplate(getType(), bundle.getString("oai"), args);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new OaiBroker(this, new OaiBrokerDefinitionAdaptor(definition));
  }
}
