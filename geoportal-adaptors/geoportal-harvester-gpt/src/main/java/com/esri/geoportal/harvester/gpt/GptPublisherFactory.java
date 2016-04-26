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
import com.esri.geoportal.harvester.api.DataDestinationFactory;
import com.esri.geoportal.harvester.api.DataDestination;
import java.util.Map;

/**
 * Gpt publisher factory.
 */
public class GptPublisherFactory implements DataDestinationFactory {

  @Override
  public DataDestination create(Map<String, String> attributes) throws IllegalArgumentException {
    GptAttributesAdaptor attr = new GptAttributesAdaptor(attributes);
    Client client = new Client(attr.getHostUrl(), attr.getUserName(), attr.getUserName());
    return new GptDataDestination(attr,client);
  }

  /*
  @Override
  public DataDestination create(InitParams initParams) {
    try {
      GptAttributesAdaptor castedInitParams = (GptAttributesAdaptor)initParams;
      Client client = new Client(castedInitParams.getHostUrl(), castedInitParams.getUserName(), castedInitParams.getUserName());
      return new GptDataDestination(castedInitParams,client);
    } catch (ClassCastException ex) {
      throw new IllegalArgumentException("Invalid init params type", ex);
    }
  }

  @Override
  public boolean canConsume(InitParams initParams) {
    return initParams instanceof GptAttributesAdaptor;
  }
*/
  
}
