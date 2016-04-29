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
package com.esri.geoportal.harvester.api.support;

import com.esri.geoportal.harvester.api.DataConnectorDefinition;
import com.esri.geoportal.harvester.api.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import java.io.IOException;
import java.io.PrintStream;
import com.esri.geoportal.harvester.api.DataOutput;

/**
 * Data print stream output.
 */
public class DataPrintStreamOutput implements DataOutput<String> {
  private final DataReferenceSerializer SERIALIZER = new DataReferenceSerializer();
  private final PrintStream out;

  /**
   * Creates instance of the destination.
   * @param out output print stream
   */
  public DataPrintStreamOutput(PrintStream out) {
    this.out = out;
  }

  @Override
  public void publish(DataReference<String> ref) throws DataOutputException {
    try {
      SERIALIZER.serialize(out, ref);
    } catch (IOException ex) {
      throw new DataOutputException(this, "Error serializing data.", ex);
    }
  }

  @Override
  public DataConnectorDefinition getDefinition() {
    DataConnectorDefinition def = new DataConnectorDefinition();
    def.setType("DataPrintStreamDestination");;
    return def;
  }

  @Override
  public void close() throws Exception {
  }
}
