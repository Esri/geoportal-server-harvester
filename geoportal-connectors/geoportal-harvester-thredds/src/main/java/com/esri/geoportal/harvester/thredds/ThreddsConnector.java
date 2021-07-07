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
package com.esri.geoportal.harvester.thredds;

import static com.esri.geoportal.harvester.thredds.ThreddsConstants.*;
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
 * THREDDS connector.
 */
public class ThreddsConnector implements InputConnector<InputBroker> {

  public static final String TYPE = "THREDDS";

  /**
   * Creates instance of the connector.
   */
  public ThreddsConnector() {
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("ThreddsResource", locale);
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("thredds.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("thredds.hint");
      }
    });
    args.add(new UITemplate.BooleanArgument(P_EMIT_XML, bundle.getString("thredds.emit.xml"),false, Boolean.TRUE));
    args.add(new UITemplate.BooleanArgument(P_EMIT_JSON, bundle.getString("thredds.emit.json"),false, Boolean.FALSE));
    return new UITemplate(getType(), bundle.getString("thredds"), args);
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new ThreddsBrokerDefinitionAdaptor(definition);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new ThreddsBroker(this, new ThreddsBrokerDefinitionAdaptor(definition));
  }

  @Override
  public String getResourceLocator(EntityDefinition definition) {
    try {
      ThreddsBrokerDefinitionAdaptor adaptor = new ThreddsBrokerDefinitionAdaptor(definition);
      return adaptor.getHostUrl()!=null? adaptor.getHostUrl().toExternalForm(): "";
    } catch (InvalidDefinitionException ex) {
      return "";
    }
  }
}
