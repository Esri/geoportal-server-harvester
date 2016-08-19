/*
 * Copyright 2016 Esri, Inc.
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
package com.esri.geoportal.harvester.api;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;

/**
 * Filter instance.
 * @see Filter
 */
public interface FilterInstance extends AutoCloseable {
  
  /**
   * Initialize instance.
   * @param task task for which the filter will be used
   * @throws DataProcessorException if initialization fails.
   */
  void initialize(Task task) throws DataProcessorException;
  
  /**
   * Terminates instance
   * @throws DataProcessorException if termination fails.
   */
  void terminate() throws DataProcessorException;
  
  /**
   * Gets filter definition.
   * @return filter definition
   */
  EntityDefinition getFilterDefinition();
  
  /**
   * Tests data if allowed by filter.
   * @param dataReference data reference
   * @return <code>true</code> if data allowed by filter
   */
  boolean test(DataReference dataReference);
}
