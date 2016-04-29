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
package com.esri.geoportal.harvester.console;

import com.esri.geoportal.harvester.api.DataConnectorDefinition;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.DataOutputException;
import java.io.IOException;
import com.esri.geoportal.harvester.api.DataOutput;

/**
 * Console data publisher.
 */
public class ConsoleDataDestination implements DataOutput<String> {
  private long counter = 0;

  /**
   * Creates instance of the destination.
   */
  public ConsoleDataDestination() {
  }

  @Override
  public DataConnectorDefinition getDefinition() {
    DataConnectorDefinition def = new DataConnectorDefinition();
    def.setType("CONSOLE");
    return def;
  }

  @Override
  public void publish(DataReference<String> ref) throws DataOutputException {
    try {
      counter++;
      
      System.out.println(String.format("%s [%s]", ref.getSourceUri(), ref.getLastModifiedDate()));
      System.out.println(String.format("%s", ref.getContent()));
      System.out.println(String.format("--- END OF %d ---", counter));
      System.out.println();
    } catch (IOException ex) {
      throw new DataOutputException(this, "Error publishing data.", ex);
    }
  }
  
  @Override
  public String toString() {
    return String.format("CONSOLE");
  }

  @Override
  public void close() throws Exception {
  }
}
