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
package com.esri.geoportal.harvester.ckan.data.gov;

import com.esri.geoportal.harvester.ckan.*;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import static com.esri.geoportal.harvester.ckan.data.gov.DataGovConstants.*;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * CKAN connector.
 */
public class DataGovConnector extends CkanConnector {

  public static final String TYPE = "DATA.GOV";

  /**
   * Creates instance of the connector.
   * @param metaBuilder meta builder
   */
  public DataGovConnector(MetaBuilder metaBuilder) {
    super(metaBuilder);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("CkanResource", locale);
    UITemplate template = super.getTemplate(locale);
    
    List<UITemplate.Argument> arguments = template.getArguments();
    UITemplate.Argument apiUrlArg = arguments.remove(0);
    arguments.add(0, new UITemplate.ArgumentWrapper(apiUrlArg){
      @Override
      public boolean getRequired() {
        return false;
      }
      @Override
      public String getHint() {
        return bundle.getString("data.gov.url.hint");
      }
    });
    arguments.add(1, new UITemplate.StringArgument(P_XML_URL, bundle.getString("data.gov.xml")){
      @Override
      public String getHint() {
        return bundle.getString("data.gov.xml.hint");
      }
    });
    arguments.add(2, new UITemplate.StringArgument(P_OID_KEY, bundle.getString("data.gov.oid")){
      @Override
      public String getHint() {
        return bundle.getString("data.gov.oid.hint");
      }
    });
    
    return new UITemplate(getType(), bundle.getString("data.gov"), arguments);
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new DataGovBrokerDefinitionAdaptor(definition);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new DataGovBroker(this, new DataGovBrokerDefinitionAdaptor(definition), metaBuilder);
  }
}
