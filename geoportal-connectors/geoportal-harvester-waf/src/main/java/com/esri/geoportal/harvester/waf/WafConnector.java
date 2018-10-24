/*
 * Copyright 2016 Piotr Andzel.
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
package com.esri.geoportal.harvester.waf;

import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_PASSWORD;
import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_USERNAME;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import static com.esri.geoportal.harvester.waf.WafConstants.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Waf connector.
 * @see com.esri.geoportal.harvester.waf API
 */
public class WafConnector implements InputConnector<InputBroker> {
  public static final String TYPE = "WAF";

  @Override
  public String getType() {
    return TYPE;
  }
  
  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("WafResource", locale);
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("waf.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("waf.url.hint");
      }
    });
    args.add(new UITemplate.StringArgument(P_PATTERN, bundle.getString("waf.pattern")){
      @Override
      public String getHint() {
        return bundle.getString("waf.pattern.hint");
      }
    });
    args.add(new UITemplate.StringArgument(P_CRED_USERNAME, bundle.getString("waf.username"), false));
    args.add(new UITemplate.StringArgument(P_CRED_PASSWORD, bundle.getString("waf.password"), false) {
      public boolean isPassword() {
        return true;
      }
    });
    return new UITemplate(getType(), bundle.getString("waf"), args);
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new WafBrokerDefinitionAdaptor(definition);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new WafBroker(this,new WafBrokerDefinitionAdaptor(definition));
  }
}
