/*
 * Copyright 2016 Esri, Inc.
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
package com.esri.geoportal.harvester.console;

import com.esri.geoportal.harvester.api.Connector;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import java.io.IOException;

/**
 * Console broker.
 */
public class ConsoleBroker implements OutputBroker  {
  private long counter = 0;
  private final ConsoleConnector connector;
  private final ConsoleBrokerDefinitionAdaptor definition;

  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   */
  public ConsoleBroker(ConsoleConnector connector, ConsoleBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public void publish(DataReference ref) throws DataOutputException {
    try {
      counter++;
      
      System.out.println(String.format("%s [%s]", ref.getId(), ref.getLastModifiedDate()));
      System.out.println(String.format("%s", new String(ref.getContent(),"UTF-8")));
      System.out.println(String.format("--- END OF %d ---", counter));
      System.out.println();
    } catch (IOException ex) {
      throw new DataOutputException(this, "Error publishing data.", ex);
    }
  }

  @Override
  public void close() throws IOException {
    // no closing neccessary
  }

  @Override
  public Connector getConnector() {
    return connector;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }
  
}
