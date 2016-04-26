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
import com.esri.geoportal.harvester.api.DataAdaptorDefinition;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.DataDestination;
import com.esri.geoportal.harvester.api.DataDestinationException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Gpt data destination.
 */
public class GptDataDestination implements DataDestination<String> {
  private final GptAttributesAdaptor attributes;
  private final Client client;

  /**
   * Creates instance of the destination
   * @param attributes attributes
   * @param client gpt rest client
   */
  public GptDataDestination(GptAttributesAdaptor attributes, Client client) {
    this.attributes = attributes;
    this.client = client;
  }

  @Override
  public DataAdaptorDefinition getDefinition() {
    DataAdaptorDefinition def = new DataAdaptorDefinition();
    def.setType("GPT");
    def.setAttributes(attributes);
    return def;
  }

  @Override
  public String getDescription() {
    return String.format("GPT [%s]", attributes.getHostUrl());
  }

  @Override
  public void publish(DataReference<String> ref) throws DataDestinationException {
    try {
      String content = ref.getContent();
      client.publish(content);
    } catch (IOException|URISyntaxException ex) {
      throw new DataDestinationException(this, "Error publishing data.", ex);
    }
  }
  
  @Override
  public String toString() {
    return getDescription();
  }

  @Override
  public void close() throws Exception {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
