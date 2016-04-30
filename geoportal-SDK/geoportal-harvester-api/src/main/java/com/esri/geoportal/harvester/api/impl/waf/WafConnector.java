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
package com.esri.geoportal.harvester.api.impl.waf;

import com.esri.geoportal.harvester.api.n.BrokerDefinition;
import com.esri.geoportal.harvester.api.n.ConnectorTemplate;
import com.esri.geoportal.harvester.api.n.InputConnector;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Piotr Andzel
 */
public class WafConnector implements InputConnector<WafBroker> {

  @Override
  public ConnectorTemplate getTemplate() {
    List<ConnectorTemplate.Argument> args = new ArrayList<>();
    args.add(new ConnectorTemplate.StringArgument("URL", "Url"));
    return new ConnectorTemplate("WAF", "Web Accessible Folder", args);
  }

  @Override
  public WafBroker createBroker(BrokerDefinition definition) {
    return new WafBroker(new WafArguments(definition));
  }
}
