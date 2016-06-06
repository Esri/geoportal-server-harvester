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
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;

/**
 * Trigger.
 */
public interface Trigger extends AutoCloseable {
  /**
   * Gets type of the trigger.
   * @return type of the trigger
   */
  String getType();
  
  /**
   * Gets UI template.
   * @return template
   */
  UITemplate getTemplate();
  
  /**
   * Creates instance of the trigger.
   * @param triggerDefinition trigger definition
   * @return instance of the trigger
   * @throws InvalidDefinitionException if trigger definition is invalid
   */
  Instance createInstance(TriggerDefinition triggerDefinition) throws InvalidDefinitionException;
  
  /**
   * Trigger instance.
   */
  interface Instance extends AutoCloseable {
    
    /**
     * Gets trigger.
     * @return trigger
     */
    Trigger getTrigger();
    
    /**
     * Activates the trigger.
     * @param triggerContext trigger context
     * @throws DataProcessorException if creating process fails
     * @throws InvalidDefinitionException if task definition is invalid
     */
    void activate(Context triggerContext) throws DataProcessorException, InvalidDefinitionException;
  }
  
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
    Processor.Process submit(TaskDefinition taskDefinition) throws DataProcessorException, InvalidDefinitionException;
    
    /**
     * Gets context variable.
     * @param <T> type of the data
     * @param varName variable name
     * @param clazz class of the data
     * @return variable value or <code>null</code> if no variable available
     */
    <T> T getEnv(String varName, Class<T> clazz);
    
    /**
     * Sets context variable.
     * @param varName variable name
     * @param var variable value or <code>null</code> to remove 
     */
    void setEnv(String varName, Object var);
  }
}
