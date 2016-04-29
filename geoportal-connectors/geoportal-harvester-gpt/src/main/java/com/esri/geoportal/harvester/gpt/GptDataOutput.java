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
import com.esri.geoportal.harvester.api.DataConnectorDefinition;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.DataOutputException;
import java.io.IOException;
import java.net.URISyntaxException;
import com.esri.geoportal.harvester.api.DataOutput;

/**
 * Gpt data output.
 */
public class GptDataOutput implements DataOutput<String> {
  private final GptAttributesAdaptor attributes;
  private final Client client;

  /**
   * Creates instance of the output
   * @param attributes attributes
   * @param client gpt rest client
   */
  public GptDataOutput(GptAttributesAdaptor attributes, Client client) {
    this.attributes = attributes;
    this.client = client;
  }

  @Override
  public DataConnectorDefinition getDefinition() {
    DataConnectorDefinition def = new DataConnectorDefinition();
    def.setType("GPT");
    def.setAttributes(attributes);
    return def;
  }

  @Override
  public void publish(DataReference<String> ref) throws DataOutputException {
    try {
      String content = ref.getContent();
      client.publish(content);
    } catch (IOException|URISyntaxException ex) {
      throw new DataOutputException(this, "Error publishing data.", ex);
    }
  }
  
  @Override
  public String toString() {
    return String.format("GPT [%s]", attributes.getHostUrl());
  }

  @Override
  public void close() throws Exception {
    client.close();
  }
}
