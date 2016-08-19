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
package com.esri.geoportal.harvester.api.specs;

import com.esri.geoportal.harvester.api.Broker;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;

/**
 * Output broker.
 * <p>
 * Provides mechanism to publish data.
 * 
 * @see OutputConnector
 */
public interface OutputBroker extends Broker<OutputConnector> {
  
  /**
   * Initialize broker.
   * @param task task for which the broker will be used
   * @throws DataProcessorException if initialization fails.
   */
  void initialize(Task task) throws DataProcessorException;
  
  /**
   * Terminates broker.
   * @throws DataProcessorException if termination fails.
   */
  void terminate() throws DataProcessorException;
  
  /**
   * Publishes data.
   * @param ref data reference
   * @return publishing status
   * @throws DataOutputException if publishing data fails
   */
  PublishingStatus publish(DataReference ref) throws DataOutputException;
}
