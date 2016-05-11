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
package com.esri.geoportal.harvester.engine;

import java.util.List;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.OutputBroker;

/**
 * Task.
 */
public class Task implements AutoCloseable {
  private final InputBroker dataSource;
  private final List<OutputBroker> dataDestinations;
  
  /**
   * Creates instance of the task.
   * @param dataSource data source
   * @param dataDestinations data destination
   */
  public Task(InputBroker dataSource, List<OutputBroker> dataDestinations) {
    this.dataSource = dataSource;
    this.dataDestinations = dataDestinations;
  }

  /**
   * Gets data source.
   * @return data source
   */
  public InputBroker getDataSource() {
    return dataSource;
  }

  /**
   * Gets data publisher.
   * @return data publisher
   */
  public List<OutputBroker> getDataDestinations() {
    return dataDestinations;
  }

  @Override
  public void close() throws Exception {
    getDataSource().close();
    for (OutputBroker d: getDataDestinations()) {
      try {
        d.close();
      } catch (Exception ex) {}
    }
  }
  
  @Override
  public String toString() {
    StringBuilder descriptions = new StringBuilder();
    dataDestinations.forEach(d->descriptions.append(descriptions.length()>0? ", ": "").append(d.toString()));
    return String.format("TASK :: %s --> [%s]", dataSource.toString(), descriptions);
  }
}
