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
package com.esri.geoportal.harvester.agpsrc;

import static com.esri.geoportal.commons.constants.CredentialsConstants.*;
import static com.esri.geoportal.harvester.agpsrc.AgpConstants.*;
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
 * ArcGIS Portal output connector.
 */
public class AgpInputConnector implements InputConnector<InputBroker> {
  public static final String TYPE = "AGP-IN";
  
  private final MetaBuilder metaBuilder;
  final String geometryServiceUrl;

  /**
   * Creates instance of the connector.
   * @param metaBuilder meta builder
   * @param geometryServiceUrl geometry service url
   */
  public AgpInputConnector(MetaBuilder metaBuilder, String geometryServiceUrl) {
    this.metaBuilder = metaBuilder;
    this.geometryServiceUrl = geometryServiceUrl;
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new AgpInputBroker(this, new AgpInputBrokerDefinitionAdaptor(definition), metaBuilder);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("AgpSrcResource", locale);
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("agpsrc.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("agpsrc.hint");
      }
    });
    args.add(new UITemplate.StringArgument(P_FOLDER_ID, bundle.getString("agpsrc.folderId"), false));
    args.add(new UITemplate.StringArgument(P_CRED_USERNAME, bundle.getString("agpsrc.username"), false));
    args.add(new UITemplate.StringArgument(P_CRED_PASSWORD, bundle.getString("agpsrc.password"), false) {
      public boolean isPassword() {
        return true;
      }
    });
    args.add(new UITemplate.BooleanArgument(P_EMIT_XML, bundle.getString("agpsrc.emit.xml"),false, Boolean.TRUE));
    args.add(new UITemplate.BooleanArgument(P_EMIT_JSON, bundle.getString("agpsrc.emit.json"),false, Boolean.FALSE));
    return new UITemplate(getType(), bundle.getString("agpsrc"), args);
  }
  
}
