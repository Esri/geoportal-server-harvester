/*
 * Copyright 2017 Esri, Inc.
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
package com.esri.geoportal.harvester.migration;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import static com.esri.geoportal.harvester.migration.MigrationConstants.P_JNDI_NAME;
import static com.esri.geoportal.harvester.migration.MigrationConstants.P_PRESERVE_UUIDS;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Migration connector.
 */
public class MigrationConnector implements InputConnector<InputBroker>{
  public static final String TYPE = "MIG";

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new MigrationBrokerDefinitionAdaptor(definition);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new MigrationBroker(this, new MigrationBrokerDefinitionAdaptor(definition));
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("MigrationResource", locale);
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.StringArgument(P_JNDI_NAME, bundle.getString("migration.jndi"), true));
    arguments.add(new UITemplate.BooleanArgument(P_PRESERVE_UUIDS, bundle.getString("migration.preserveuuids"), true));
    return new UITemplate(getType(), bundle.getString("migration"), arguments);
  }

  @Override
  public String getResourceLocator(EntityDefinition definition) {
    try {
      MigrationBrokerDefinitionAdaptor adaptor = new MigrationBrokerDefinitionAdaptor(definition);
      return adaptor.getJndi()!=null? adaptor.getJndi(): "";
    } catch (InvalidDefinitionException ex) {
      return "";
    }
  }
}
