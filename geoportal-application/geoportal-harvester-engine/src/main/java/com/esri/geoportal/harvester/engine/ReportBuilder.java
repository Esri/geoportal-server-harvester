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

import com.esri.geoportal.harvester.api.DataDestinationException;
import com.esri.geoportal.harvester.api.DataProcessorException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.DataSourceException;

/**
 * Report builder.
 */
public interface ReportBuilder {
  /**
   * Called to indicate beginning of the process.
   * @param process process
   */
  void started(Process process);
  
  /**
   * Called to indicated completion of the process
   * @param process process
   */
  void completed(Process process);
  
  /**
   * Make success entry.
   * @param process process
   * @param dataReference data reference
   */
  void success(Process process, DataReference dataReference);
  
  /**
   * Make harvest error entry.
   * @param process process
   * @param ex exception
   */
  void error(Process process, DataSourceException ex);
  
  /**
   * Make publish error entry.
   * @param process process
   * @param ex exception
   */
  void error(Process process, DataDestinationException ex);
  
  /**
   * Make error entry without associated data.
   * @param process process
   * @param ex error message
   */
  void error(Process process, DataProcessorException ex);
}