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
package com.esri.geoportal.harvester.impl;

import com.esri.geoportal.harvester.api.DataDestination;
import com.esri.geoportal.harvester.api.DataDestinationException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.DataSource;
import com.esri.geoportal.harvester.api.DataSourceException;
import java.util.List;

/**
 * Data collector.
 * @param <T> type of data
 */
public class DataCollector<T> {
  private final DataSource<T> source;
  private final List<DataDestination<T>> destinations;

  /**
   * Creates instance of the collector.
   * @param source data source
   * @param destinations data destinations
   */
  public DataCollector(DataSource<T> source, List<DataDestination<T>> destinations) {
    this.source = source;
    this.destinations = destinations;
  }
  
  /**
   * Collect available data.
   */
  public void collect() {
    onStart();
    
    try {
      top: while (source.hasNext()) {
        if (Thread.currentThread().isInterrupted()) break;
        DataReference<T> dataReference = source.next();
        for (DataDestination<T> d: destinations) {
          if (Thread.currentThread().isInterrupted()) break top;
          try {
            d.publish(dataReference);
          } catch (DataDestinationException ex) {
            onDestinationException(ex);
          }
        }
      }
    } catch (DataSourceException ex) {
      onSourceException(ex);
    } finally {
      onComplete();
    }
  }
  
  protected void onStart() {}
  protected void onComplete() {}
  protected void onSourceException(DataSourceException ex) {}
  protected void onDestinationException(DataDestinationException ex) {}
}
