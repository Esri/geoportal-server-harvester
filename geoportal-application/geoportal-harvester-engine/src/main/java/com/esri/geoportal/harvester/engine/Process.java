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

import com.esri.geoportal.harvester.api.DataDestination;
import com.esri.geoportal.harvester.api.DataDestinationException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.DataSourceException;

/**
 * Process.
 */
public class Process {
  private final ReportBuilder reportBuilder;
  private final Task<String> task;
  private Thread thread;

  /**
   * Creates instance of the process.
   * @param reportBuilder report builder
   * @param task task
   */
  public Process(ReportBuilder reportBuilder, Task<String> task) {
    this.reportBuilder = reportBuilder;
    this.task = task;
  }
  
  /**
   * Gets process description.
   * @return process description
   */
  public String getDescription() {
    return String.format("%s (%s)", task, getStatus());
  }
  
  /**
   * Gets process status.
   * @return process status
   */
  public synchronized Status getStatus() {
    if (thread==null) return Status.initialized;
    if (thread.isAlive()) return Status.working;
    return Status.completed;
  }
  
  /**
   * Begins the process.
   */
  public synchronized void begin() {
    if (getStatus()!=Status.initialized) {
      throw new IllegalStateException(String.format("Error begininig the process: process is in %s state", getStatus()));
    }
    
    thread = new Thread() {
      @Override
      public void run() {
        try {
          if (!task.getDataDestinations().isEmpty()) {
            reportBuilder.started(Process.this);
            while(task.getDataSource().hasNext()) {
              DataReference<String> dataReference = task.getDataSource().next();
              for (DataDestination<String> d: task.getDataDestinations()) {
                try {
                  d.publish(dataReference);
                  reportBuilder.success(Process.this,dataReference);
                } catch (DataDestinationException ex) {
                  reportBuilder.publishError(Process.this,ex);
                }
              }
            }
          }
        } catch (DataSourceException ex) {
          reportBuilder.harvestError(Process.this,ex);
          abort();
        } finally {
          reportBuilder.completed(Process.this);
        }
      }
    };
    
    thread.setDaemon(true);
    thread.setName("Harvesting");
    thread.start();
  }
  
  /**
   * Aborts the process.
   */
  public synchronized void abort() {
    if (getStatus()!=Status.working) {
      throw new IllegalStateException(String.format("Error aborting the process: process is in %s state", getStatus()));
    }
    thread.interrupt();
    try {
      thread.join();
    } catch (InterruptedException ex) {
      // ignore
    }
  }

  /**
   * Gets task.
   * @return task
   */
  public Task getTask() {
    return task;
  }
  
  @Override
  public String toString() {
    return String.format("PROCESS:: status: %s, task: %s", getStatus(), task);
  }
  
}
