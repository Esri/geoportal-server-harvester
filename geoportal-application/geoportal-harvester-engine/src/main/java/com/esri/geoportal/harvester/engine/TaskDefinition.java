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

import com.esri.geoportal.harvester.api.DataAdaptorDefinition;
import java.util.List;

/**
 * Process definition.
 */
public class TaskDefinition {
  private DataAdaptorDefinition source;
  private List<DataAdaptorDefinition> destinations;

  /**
   * Gets source definition.
   * @return source definition
   */
  public DataAdaptorDefinition getSource() {
    return source;
  }

  /**
   * Sets source definition.
   * @param source source definition
   */
  public void setSource(DataAdaptorDefinition source) {
    this.source = source;
  }

  /**
   * Gets destinations.
   * @return destinations
   */
  public List<DataAdaptorDefinition> getDestinations() {
    return destinations;
  }

  /**
   * Sets destinations.
   * @param destinations destinations
   */
  public void setDestinations(List<DataAdaptorDefinition> destinations) {
    this.destinations = destinations;
  }
  
  
}
