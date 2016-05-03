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

import com.esri.geoportal.harvester.api.BrokerDefinition;
import com.esri.geoportal.harvester.api.ConnectorTemplate;
import com.esri.geoportal.harvester.api.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.OutputConnector;
import static com.esri.geoportal.harvester.folder.FolderBrokerDefinitionAdaptor.P_HOST_URL;
import static com.esri.geoportal.harvester.folder.FolderBrokerDefinitionAdaptor.P_ROOT_FOLDER;
import java.util.ArrayList;
import java.util.List;

/**
 * Folder connector.
 */
public class FolderConnector implements OutputConnector<FolderBroker> {
  public static final String TYPE = "FOLDER";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public ConnectorTemplate getTemplate() {
    List<ConnectorTemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new ConnectorTemplate.StringArgument(P_ROOT_FOLDER, "Root folder"));
    arguments.add(new ConnectorTemplate.StringArgument(P_HOST_URL, "Source host URL"));
    return new ConnectorTemplate("FOLDER", "Folder", arguments);
  }

  @Override
  public FolderBroker createBroker(BrokerDefinition definition) throws InvalidDefinitionException {
    return new FolderBroker(this,new FolderBrokerDefinitionAdaptor(definition));
  }
  
}
