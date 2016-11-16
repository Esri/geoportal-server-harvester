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

import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.ex.DataException;

/**
 * Process instance. 
 * Instance of the harvesting process. In the most basic implementation it allows to start harvesting
 * and abort harvesting if needed. It is associated with a single 
 * {@link com.esri.geoportal.harvester.api.specs.InputBroker} and one or more
 * {@link com.esri.geoportal.harvester.api.specs.OutputBroker}'s.
 * @see com.esri.geoportal.harvester.api API
 */
public interface ProcessInstance {

  /**
   * Gets task.
   * @return task
   */
  Task getTask();
  
  /**
   * Initialize process.
   */
  void init();

  /**
   * Begins the process.
   */
  void begin();

  /**
   * Aborts the process.
   */
  void abort();

  /**
   * Gets process status.
   *
   * @return process status
   */
  Status getStatus();

  /**
   * Adds listened.
   *
   * @param listener listener
   */
  void addListener(Listener listener);


  /**
   * Process status.
   */
  enum Status {
    /**
     * just submitted
     */
    submitted,
    /**
     * currently being executing
     */
    working,
    /**
     * aborting
     */
    aborting,
    /**
     * completed (with or without errors or aborted)
     */
    completed
  }

  /**
   * Process listener.
   */
  interface Listener {

    /**
     * Called when status changed
     *
     * @param status new status
     */
    void onStatusChange(Status status);

    /**
     * Called when data has been acquired.
     * @param dataReference data reference
     */
    void onDataAcquired(DataReference dataReference);
    
    /**
     * Called when data reference has been processed
     *
     * @param dataReference data reference
     * @param status publishing status
     */
    void onDataProcessed(DataReference dataReference, PublishingStatus status);

    /**
     * Called for output onError.
     *
     * @param ex onError
     */
    public void onError(DataException ex);
  }
  
}
