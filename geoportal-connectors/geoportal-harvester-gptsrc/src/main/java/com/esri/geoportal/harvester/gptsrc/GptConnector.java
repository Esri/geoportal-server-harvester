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
package com.esri.geoportal.harvester.gptsrc;

import static com.esri.geoportal.commons.constants.CredentialsConstants.*;
import static com.esri.geoportal.harvester.gptsrc.GptConstants.*;
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
 * GPT connector.
 * @see com.esri.geoportal.harvester.gpt API
 */
public class GptConnector implements InputConnector<InputBroker> {
  public static final String TYPE = "GPTSRC";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("GptSrcResource", locale);
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("gptsrc.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("gptsrc.hint");
      }
    });
    arguments.add(new UITemplate.StringArgument(P_CRED_USERNAME, bundle.getString("gptsrc.username"), true));
    arguments.add(new UITemplate.StringArgument(P_CRED_PASSWORD, bundle.getString("gptsrc.password"), true) {
      public boolean isPassword() {
        return true;
      }
    });
    arguments.add(new UITemplate.StringArgument(P_INDEX, bundle.getString("gptsrc.index")));
    return new UITemplate(getType(), bundle.getString("gptsrc"), arguments);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new GptBroker(this, new GptBrokerDefinitionAdaptor(definition));
  }
  
}
