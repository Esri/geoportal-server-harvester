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
package com.esri.geoportal.harvester.ckan;

import static com.esri.geoportal.harvester.ckan.CkanConstants.*;
import com.esri.geoportal.commons.meta.MetaBuilder;
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
 * CKAN connector.
 */
public class CkanConnector implements InputConnector<InputBroker> {

  public static final String TYPE = "CKAN";
  
  private final MetaBuilder metaBuilder;

  /**
   * Creates instance of the connector.
   * @param metaBuilder meta builder
   */
  public CkanConnector(MetaBuilder metaBuilder) {
    this.metaBuilder = metaBuilder;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("CkanResource", locale);
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("ckan.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("ckan.hint");
      }
    });
    args.add(new UITemplate.StringArgument(P_API_KEY, bundle.getString("ckan.apiKey")));
    return new UITemplate(getType(), bundle.getString("ckan"), args);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new CkanBroker(this, new CkanBrokerDefinitionAdaptor(definition), metaBuilder);
  }
}
