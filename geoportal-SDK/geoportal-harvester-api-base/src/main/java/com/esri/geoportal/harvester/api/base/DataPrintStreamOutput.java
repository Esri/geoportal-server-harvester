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
package com.esri.geoportal.harvester.api.base;

import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import java.io.IOException;
import java.io.PrintStream;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;

/**
 * Data print stream output.
 */
public class DataPrintStreamOutput implements OutputBroker {
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
  public void initialize(InitContext context) throws DataProcessorException {
    // nothing to initialize
  }

  @Override
  public void terminate() {
    // nothing to terminate
  }

  @Override
  public PublishingStatus publish(DataReference ref) throws DataOutputException {
    try {
      SERIALIZER.serialize(out, ref);
      return PublishingStatus.CREATED;
    } catch (IOException ex) {
      throw new DataOutputException(this, String.format("Error publishing data: %s", ref), ex);
    }
  }

  @Override
  public OutputConnector getConnector() {
    return null;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return null;
  }
  
}
