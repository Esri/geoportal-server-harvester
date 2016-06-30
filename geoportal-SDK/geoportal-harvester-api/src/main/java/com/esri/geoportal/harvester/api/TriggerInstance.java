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

import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.util.Date;

/**
 * Trigger instance.
 */
public interface TriggerInstance extends AutoCloseable {

  /**
   * Gets trigger instance definition.
   * @return trigger instance definition
   */
  TriggerDefinition getTriggerDefinition();

  /**
   * Activates the trigger.
   * @param triggerContext trigger context
   * @throws DataProcessorException if creating process fails
   * @throws InvalidDefinitionException if task definition is invalid
   */
  void activate(Context triggerContext) throws DataProcessorException, InvalidDefinitionException;
  
  /**
   * Trigger context
   */
  interface Context {
    /**
     * Submits task definition to create and start new process.
     * @param taskDefinition task definition
     * @return instance of the process
     * @throws DataProcessorException if processing fails
     * @throws InvalidDefinitionException if task definition is invalid
     */
    ProcessInstance submit(TaskDefinition taskDefinition) throws DataProcessorException, InvalidDefinitionException;
    
    /**
     * Gets last harvest for task.
     * @return last harvest date or <code>null</code> if no last harvest date
     * @throws DataProcessorException if getting last harvest date fails
     */
    Date lastHarvest() throws DataProcessorException;
  }
}
