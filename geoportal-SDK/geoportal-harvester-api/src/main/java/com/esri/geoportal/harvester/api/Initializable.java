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

import com.esri.geoportal.harvester.api.ProcessInstance.Listener;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import java.util.Map;

/**
 * Initializable.
 */
public interface Initializable {

  /**
   * Initialize broker.
   * @param context broker context
   * @throws DataProcessorException if initialization fails.
   */
  void initialize(InitContext context) throws DataProcessorException;

  /**
   * Terminates broker.
   */
  void terminate();
  
  /**
   * Broker initialization context.
   */
  interface InitContext {
    /**
     * Gets task.
     * @return task
     */
    Task getTask();
    
    /**
     * Get parameters to override.
     * @return map of parameters to override
     */
    Map<String,String> getParams();
    
    /**
     * Adds process listener.
     * @param listener listener
     */
    void addListener(Listener listener);
  }
}
