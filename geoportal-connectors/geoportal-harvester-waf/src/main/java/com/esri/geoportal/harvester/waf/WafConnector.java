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

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import static com.esri.geoportal.harvester.waf.WafBrokerDefinitionAdaptor.P_HOST_URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Waf connector.
 */
public class WafConnector implements InputConnector<WafBroker> {
  public static final String TYPE = "WAF";

  @Override
  public String getType() {
    return TYPE;
  }
  
  @Override
  public UITemplate getTemplate() {
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_HOST_URL, "Url", true));
    return new UITemplate("WAF", "Web Accessible Folder", args);
  }

  @Override
  public WafBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new WafBroker(this,new WafBrokerDefinitionAdaptor(definition));
  }
}
