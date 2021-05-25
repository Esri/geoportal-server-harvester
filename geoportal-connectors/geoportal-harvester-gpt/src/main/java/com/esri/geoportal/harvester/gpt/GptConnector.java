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
package com.esri.geoportal.harvester.gpt;

import static com.esri.geoportal.commons.constants.CredentialsConstants.*;
import static com.esri.geoportal.harvester.gpt.GptConstants.*;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;

/**
 * GPT connector.
 * @see com.esri.geoportal.harvester.gpt API
 */
public class GptConnector implements OutputConnector<OutputBroker> {
  public static final String TYPE = "GPT";
  private static final String DEFAULT_GEOMETRY_SERVICE = "https://utility.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer";

  private final String geometryServiceUrl;
  private final Integer sizeLimit;

  public GptConnector(Integer sizeLimit) {
    this.geometryServiceUrl = DEFAULT_GEOMETRY_SERVICE;
    this.sizeLimit = sizeLimit;
  }

  public GptConnector(String geometryServiceUrl, Integer sizeLimit) {
    this.geometryServiceUrl = StringUtils.defaultIfBlank(geometryServiceUrl, DEFAULT_GEOMETRY_SERVICE);
    this.sizeLimit = sizeLimit;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("GptResource", locale);
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("gpt.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("gpt.hint");
      }
    });
    arguments.add(new UITemplate.StringArgument(P_CRED_USERNAME, bundle.getString("gpt.username"), true));
    arguments.add(new UITemplate.StringArgument(P_CRED_PASSWORD, bundle.getString("gpt.password"), true) {
      public boolean isPassword() {
        return true;
      }
    });
    arguments.add(new UITemplate.StringArgument(P_INDEX, bundle.getString("gpt.index")));
    arguments.add(new UITemplate.BooleanArgument(P_CLEANUP, bundle.getString("gpt.cleanup")));
    arguments.add(new UITemplate.BooleanArgument(P_ACCEPT_XML, bundle.getString("gpt.accept.xml"),false, Boolean.TRUE));
    arguments.add(new UITemplate.BooleanArgument(P_ACCEPT_JSON, bundle.getString("gpt.accept.json"),false, Boolean.FALSE));
    arguments.add(new UITemplate.BooleanArgument(P_TRANSLATE_PDF, bundle.getString("gpt.translate.pdf"),false, Boolean.TRUE));
    arguments.add(new UITemplate.BooleanArgument(P_EDITABLE, bundle.getString("gpt.editable"),false, Boolean.FALSE));
    arguments.add(new UITemplate.StringArgument(P_COLLECTIONS, bundle.getString("gpt.collections")) {
      @Override
      public String getHint() {
        return bundle.getString("gpt.collections.hint");
      }
    });
    arguments.add(new UITemplate.StringArgument(P_COLLECTIONS_FLD, bundle.getString("gpt.collections.fld")) {
      @Override
      public String getHint() {
        return String.format(bundle.getString("gpt.collections.fld.hint"), GptBroker.DEFAULT_COLLECTIONS_FIELD_NAME);
      }
    });
    return new UITemplate(getType(), bundle.getString("gpt"), arguments);
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new GptBrokerDefinitionAdaptor(definition);
  }

  @Override
  public OutputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new GptBroker(this, new GptBrokerDefinitionAdaptor(definition), geometryServiceUrl, sizeLimit);
  }
  
}
