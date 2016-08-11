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
package com.esri.geoportal.harvester.api.general;

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.OutputBroker.OutputBrokerContext;
import java.util.List;

/**
 * Link action.
 */
public interface LinkAction extends AutoCloseable {
  
  /**
   * Initialize link action
   * @param context context
   * @throws DataProcessorException if initialization fails.
   */
  void initialize(OutputBrokerContext context) throws DataProcessorException;
  
  /**
   * Terminates link action
   * @throws DataProcessorException if termination fails.
   */
  void terminate() throws DataProcessorException;
  
  /**
   * Gets link action definition.
   * @return link action definition
   */
  EntityDefinition getLinkActionDefinition();
  /**
   * Process actions.
   * @param dataRef data reference to process
   * @return result
   * @throws DataProcessorException if processing fails
   * @throws DataOutputException if output fails
   */
  List<DataReference> execute(DataReference dataRef) throws DataProcessorException, DataOutputException;
  
  /**
   * Pushes data reference through the link.
   * @param dataRef data reference.
   * @return publishing status
   * @throws DataProcessorException if processing fails
   * @throws DataOutputException if sending to the output fails
   */
  PublishingStatus push(DataReference dataRef) throws DataProcessorException, DataOutputException;
}
