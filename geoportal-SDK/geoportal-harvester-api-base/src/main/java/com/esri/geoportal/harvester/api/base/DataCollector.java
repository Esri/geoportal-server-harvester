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
import com.esri.geoportal.harvester.api.ex.DataInputException;
import java.util.List;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.OutputBroker;

/**
 * Data collector.
 */
public class DataCollector {
  private final InputBroker inputBroker;
  private final List<OutputBroker> outputBrokers;

  /**
   * Creates instance of the collector.
   * @param source data source
   * @param outputBrokers data destinations
   */
  public DataCollector(InputBroker source, List<OutputBroker> outputBrokers) {
    this.inputBroker = source;
    this.outputBrokers = outputBrokers;
  }
  
  /**
   * Collect available data.
   */
  public void collect() {
    onStart();
    
    try {
      top: while (inputBroker.hasNext()) {
        if (Thread.currentThread().isInterrupted()) break;
        DataReference dataReference = inputBroker.next();
        for (OutputBroker d: outputBrokers) {
          if (Thread.currentThread().isInterrupted()) break top;
          try {
            d.publish(dataReference);
          } catch (DataOutputException ex) {
            onDestinationException(ex);
          }
        }
      }
    } catch (DataInputException ex) {
      onSourceException(ex);
    } finally {
      onComplete();
    }
  }
  
  protected void onStart() {}
  protected void onComplete() {}
  protected void onSourceException(DataInputException ex) {}
  protected void onDestinationException(DataOutputException ex) {}
}
