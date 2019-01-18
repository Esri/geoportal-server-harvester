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
package com.esri.geoportal.harvester.agp;

import static com.esri.geoportal.commons.constants.CredentialsConstants.*;
import static com.esri.geoportal.harvester.agp.AgpConstants.*;
import com.esri.geoportal.commons.meta.MetaAnalyzer;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * ArcGIS Portal output connector.
 */
public class AgpOutputConnector implements OutputConnector<OutputBroker> {
  public static final String TYPE = "AGP-OUT";
  
  private final MetaAnalyzer metaAnalyzer;

  /**
   * Creates instance of the connector.
   * @param metaAnalyzer meta analyzer
   */
  public AgpOutputConnector(MetaAnalyzer metaAnalyzer) {
    this.metaAnalyzer = metaAnalyzer;
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new AgpOutputBrokerDefinitionAdaptor(definition);
  }

  @Override
  public OutputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new AgpOutputBroker(this, new AgpOutputBrokerDefinitionAdaptor(definition), metaAnalyzer);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("AgpResource", locale);
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("agp.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("agp.hint");
      }
    });
    args.add(new UITemplate.StringArgument(P_FOLDER_ID, bundle.getString("agp.folderId"), false));
    args.add(new UITemplate.StringArgument(P_CRED_USERNAME, bundle.getString("agp.username"), true));
    args.add(new UITemplate.StringArgument(P_CRED_PASSWORD, bundle.getString("agp.password"), true) {
      public boolean isPassword() {
        return true;
      }
    });
    args.add(new UITemplate.IntegerArgument(P_MAX_REDIRECTS, bundle.getString("agp.max.redirects"), false, 5));
    args.add(new UITemplate.BooleanArgument(P_FOLDER_CLEANUP, bundle.getString("agp.cleanup")));
    args.add(new UITemplate.BooleanArgument(P_UPLOAD, bundle.getString("agp.upload")));
    return new UITemplate(getType(), bundle.getString("agp"), args);
  }
  
}
