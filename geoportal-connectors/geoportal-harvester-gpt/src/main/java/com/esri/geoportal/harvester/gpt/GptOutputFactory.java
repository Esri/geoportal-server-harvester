/*
 * Copyright 2016 Esri, Inc..
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

import com.esri.geoportal.commons.gpt.client.Client;
import com.esri.geoportal.harvester.api.DataConnectorTemplate;
import static com.esri.geoportal.harvester.gpt.GptAttributesAdaptor.P_HOST_URL;
import static com.esri.geoportal.harvester.gpt.GptAttributesAdaptor.P_USER_NAME;
import static com.esri.geoportal.harvester.gpt.GptAttributesAdaptor.P_USER_PASSWORD;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.esri.geoportal.harvester.api.DataOutput;
import com.esri.geoportal.harvester.api.DataOutputFactory;

/**
 * Gpt output factory.
 */
public class GptOutputFactory implements DataOutputFactory {

  @Override
  public DataOutput create(Map<String, String> attributes) throws IllegalArgumentException {
    GptAttributesAdaptor attr = new GptAttributesAdaptor(attributes);
    Client client = new Client(attr.getHostUrl(), attr.getUserName(), attr.getUserName());
    return new GptDataOutput(attr,client);
  }

  @Override
  public DataConnectorTemplate getTemplate() {
    List<DataConnectorTemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new DataConnectorTemplate.StringArgument(P_HOST_URL, "URL"));
    arguments.add(new DataConnectorTemplate.StringArgument(P_USER_NAME, "User name"));
    arguments.add(new DataConnectorTemplate.StringArgument(P_USER_PASSWORD, "User password") {
      public boolean isPassword() {
        return true;
      }
    });
    return new DataConnectorTemplate("GPT", "Geoportal Server New Generation", arguments);
  }

  
}
