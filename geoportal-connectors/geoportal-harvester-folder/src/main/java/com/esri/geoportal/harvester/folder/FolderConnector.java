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
package com.esri.geoportal.harvester.folder;

import static com.esri.geoportal.harvester.folder.FolderConstants.*;
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
 * Folder connector.
 * @see com.esri.geoportal.harvester.folder API
 */
public class FolderConnector implements OutputConnector<OutputBroker> {
  public static final String TYPE = "FOLDER";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("FolderResource", locale);
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.StringArgument(P_ROOT_FOLDER, bundle.getString("folder.rootFolder"), true){
      @Override
      public String getHint() {
        return bundle.getString("folder.hint");
      }
    });
    arguments.add(new UITemplate.BooleanArgument(P_FOLDER_CLEANUP, bundle.getString("folder.cleanup")));
    return new UITemplate(getType(), bundle.getString("folder"), arguments);
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new FolderBrokerDefinitionAdaptor(definition);
  }

  @Override
  public OutputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new FolderBroker(this,new FolderBrokerDefinitionAdaptor(definition));
  }

  @Override
  public String getResourceLocator(EntityDefinition definition) {
    try {
      FolderBrokerDefinitionAdaptor adaptor = new FolderBrokerDefinitionAdaptor(definition);
      return adaptor.getRootFolder()!=null? adaptor.getRootFolder().toString(): "";
    } catch (InvalidDefinitionException ex) {
      return "";
    }
  }
  
}
