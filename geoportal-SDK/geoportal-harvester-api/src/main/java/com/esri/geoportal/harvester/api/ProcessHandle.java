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

import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;

/**
 * ProcessHandle.
 */
public interface ProcessHandle {
  /**
   * Gets process title.
   * @return process title
   */
  String getTitle();

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
   * @return process status
   */
  Status getStatus();
  
  /**
   * Adds listened.
   * @param listener listener
   */
  void addListener(Listener listener);
  
  /**
   * ProcessHandle status.
   */
  enum Status {
    /** just submitted */
    submitted, 
    /** currently being executing */
    working, 
    /** aborting */
    aborting,
    /** completed (with or without errors or aborted) */
    completed
  }
    
  /**
   * ProcessHandle listener.
   */
  interface Listener {
    /**
     * Called when status changed
     * @param newStatus new status
     */
    void onStatus(Status newStatus);
    
    /**
     * Called when data reference has been processed
     * @param dataReference data reference
     */
    void onSuccess(DataReference dataReference);
    
    /**
     * Called for output onError.
     * @param ex onError
     */
    public void onError(DataOutputException ex);
    
    /**
     * Called for input onError.
     * @param ex onError
     */
    public void onError(DataInputException ex);
  }

  
}
