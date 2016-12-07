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
import com.esri.geoportal.harvester.api.general.Entity;
import com.esri.geoportal.harvester.api.specs.InputBroker;

/**
 * Processor.
 * Provides a way to execute harvesting process.In the most basic implementation it will iterate
 * through input data using {@link com.esri.geoportal.harvester.api.specs.InputConnector}
 * iteration pattern methods and push each data to the output using 
 * {@link com.esri.geoportal.harvester.api.specs.OutputConnector} interface.
 * @see com.esri.geoportal.harvester.api API
 */
public interface Processor extends Entity {
  /**
   * Gets processor definition.
   * @return processor definition
   */
  EntityDefinition getEntityDefinition();

  /**
   * Creates process.
   * @param task task
   * @param iteratorContext iterator context
   * @return instance of the process
   */
  ProcessInstance createProcess(Task task, InputBroker.IteratorContext iteratorContext);

}
