/*
 * Copyright 2019 Esri, Inc.
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
package com.esri.geoportal.harvester.dcat;

import static com.esri.geoportal.harvester.dcat.DcatConstants.*;
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
 * DCAT connector.
 */
public class DcatConnector implements InputConnector<InputBroker> {

  public static final String TYPE = "DCAT";
  
  protected final MetaBuilder metaBuilder;

  /**
   * Creates instance of the connector.
   * @param metaBuilder meta builder
   */
  public DcatConnector(MetaBuilder metaBuilder) {
    this.metaBuilder = metaBuilder;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("DcatResource", locale);
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("dcat.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("ckan.hint");
      }
    });
    args.add(new UITemplate.BooleanArgument(P_EMIT_XML, bundle.getString("dcat.emit.xml"),false, Boolean.TRUE));
    args.add(new UITemplate.BooleanArgument(P_EMIT_JSON, bundle.getString("dcat.emit.json"),false, Boolean.TRUE));
    return new UITemplate(getType(), bundle.getString("dcat"), args);
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new DcatBrokerDefinitionAdaptor(definition);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new DcatBroker(this, new DcatBrokerDefinitionAdaptor(definition), metaBuilder);
  }
}
