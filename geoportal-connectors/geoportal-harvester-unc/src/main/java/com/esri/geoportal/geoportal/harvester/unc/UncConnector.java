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
package com.esri.geoportal.geoportal.harvester.unc;

import static com.esri.geoportal.geoportal.harvester.unc.UncBrokerDefinitionAdaptor.P_PATTERN;
import static com.esri.geoportal.geoportal.harvester.unc.UncBrokerDefinitionAdaptor.P_ROOT_FOLDER;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.util.ArrayList;
import java.util.List;

/**
 * UNC connector.
 * @see com.esri.geoportal.harvester.waf API
 */
public class UncConnector implements InputConnector<InputBroker> {
  public static final String TYPE = "UNC";

  @Override
  public String getType() {
    return TYPE;
  }
  
  @Override
  public UITemplate getTemplate() {
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_ROOT_FOLDER, "Root Folder", true));
    args.add(new UITemplate.StringArgument(P_PATTERN, "Pattern"));
    return new UITemplate(getType(), "UNC Path", args);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new UncBroker(this, new UncBrokerDefinitionAdaptor(definition));
  }
  
}
