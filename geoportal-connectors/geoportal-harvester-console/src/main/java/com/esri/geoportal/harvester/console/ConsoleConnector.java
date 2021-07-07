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
package com.esri.geoportal.harvester.console;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Console connector.
 * @see com.esri.geoportal.harvester.console API
 */
public class ConsoleConnector implements OutputConnector<OutputBroker> {
  public static final String TYPE = "CONSOLE";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("ConsoleResource", locale);
    return new UITemplate(getType(), bundle.getString("console"), null);
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new ConsoleBrokerDefinitionAdaptor(definition);
  }

  @Override
  public OutputBroker createBroker(EntityDefinition entityDefinition) throws InvalidDefinitionException {
    return new ConsoleBroker(this, new ConsoleBrokerDefinitionAdaptor(entityDefinition));
  }

  @Override
  public String getResourceLocator(EntityDefinition definition) {
    try {
      new ConsoleBrokerDefinitionAdaptor(definition);
      return "console";
    } catch (InvalidDefinitionException ex) {
      return "";
    }
  }
  
}
