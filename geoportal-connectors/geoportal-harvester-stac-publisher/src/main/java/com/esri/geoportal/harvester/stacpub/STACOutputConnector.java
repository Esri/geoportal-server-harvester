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
package com.esri.geoportal.harvester.stacpub;

import static com.esri.geoportal.commons.constants.CredentialsConstants.*;
import static com.esri.geoportal.harvester.stacpub.STACConstants.*;
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
 * ArcGIS Portal output connector.
 */
public class STACOutputConnector implements OutputConnector<OutputBroker> {
  public static final String TYPE = "STAC-OUT";
  private static final String DEFAULT_GEOMETRY_SERVICE = "https://utility.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer";
  
  private final String geometryServiceUrl;
  private final Integer sizeLimit;

  /**
   * Creates instance of the connector.
   * @param metaAnalyzer meta analyzer
   * @param geometryServiceUrl geometry service URL
   * @param sizeLimit TIKA size limit
   */
  public STACOutputConnector(String geometryServiceUrl, Integer sizeLimit) {
    this.geometryServiceUrl = StringUtils.defaultIfBlank(geometryServiceUrl, DEFAULT_GEOMETRY_SERVICE);
    this.sizeLimit = sizeLimit;
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new STACOutputBrokerDefinitionAdaptor(definition);
  }

  @Override
  public OutputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new STACOutputBroker(this, new STACOutputBrokerDefinitionAdaptor(definition));
  }

  @Override
  public String getResourceLocator(EntityDefinition definition) {
    try {
      STACOutputBrokerDefinitionAdaptor adaptor = new STACOutputBrokerDefinitionAdaptor(definition);
      return adaptor.getHostUrl()!=null? adaptor.getHostUrl().toExternalForm(): "";
    } catch (InvalidDefinitionException ex) {
      return "";
    }
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("STACResource", locale);
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_HOST_URL, bundle.getString("stac.url"), true){
      @Override
      public String getHint() {
        return bundle.getString("stac.hint");
      }
    });
    args.add(new UITemplate.StringArgument(P_FOLDER_ID, bundle.getString("stac.folderId"), false));       
    args.add(new UITemplate.StringArgument(P_CRED_USERNAME, bundle.getString("stac.username"), true));
    args.add(new UITemplate.StringArgument(P_CRED_PASSWORD, bundle.getString("stac.password"), true) {
      public boolean isPassword() {
        return true;
      }
    });
    args.add(new UITemplate.IntegerArgument(P_MAX_REDIRECTS, bundle.getString("stac.max.redirects"), false, 5));
    args.add(new UITemplate.BooleanArgument(P_FOLDER_CLEANUP, bundle.getString("stac.cleanup")));
    args.add(new UITemplate.BooleanArgument(P_UPLOAD, bundle.getString("stac.upload"), true, true));
    args.add(new UITemplate.BooleanArgument(P_MARKDOWN2HTML, bundle.getString("stac.markdown2html"), true, true));
    args.add(new UITemplate.BooleanArgument(P_OAUTH, bundle.getString("stac.oauth"), false,false)); 
    args.add(new UITemplate.HiddenArgument(P_TOKEN, "hidden"));
    return new UITemplate(getType(), bundle.getString("stac"), args);
  }
  
}
