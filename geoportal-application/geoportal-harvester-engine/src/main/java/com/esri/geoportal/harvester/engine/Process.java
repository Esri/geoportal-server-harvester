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

import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.Processor.Handler;
import java.util.Map;

/**
 * Process.
 */
public class Process {
  private final ReportBuilder reportBuilder;
  private final Processor processor;
  private final Map<String,String> processorEnv;
  private final Task<String> task;
  
  private Handler handler;

  /**
   * Creates instance of the process.
   * @param reportBuilder report builder
   * @param processor processor
   * @param processorEnv processor variables
   * @param task task
   */
  public Process(ReportBuilder reportBuilder, Processor processor, Map<String,String> processorEnv, Task<String> task) {
    this.reportBuilder = reportBuilder;
    this.processor = processor;
    this.processorEnv = processorEnv;
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
    if (handler==null) return Status.initialized;
    if (handler.isActive()) return Status.working;
    return Status.completed;
  }
  
  /**
   * Begins the process.
   */
  public synchronized void begin() {
    if (getStatus()!=Status.initialized) {
      throw new IllegalStateException(String.format("Error begininig the process: process is in %s state", getStatus()));
    }
    
    handler = processor.initialize(task.getDataSource(), task.getDataDestinations(), new DefaultProcessor.Listener<String>() {
      @Override
      public void onStarted() {
        reportBuilder.started(Process.this);
      }

      @Override
      public void onCompleted() {
        reportBuilder.completed(Process.this);
      }

      @Override
      public void onSuccess(DataReference<String> dataReference) {
        reportBuilder.success(Process.this, dataReference);
      }

      @Override
      public void onError(DataOutputException ex) {
        reportBuilder.error(Process.this, ex);
      }

      @Override
      public void onError(DataInputException ex) {
        reportBuilder.error(Process.this, ex);
      }
    });
  }
  
  /**
   * Aborts the process.
   */
  public synchronized void abort() {
    if (getStatus()!=Status.working) {
      throw new IllegalStateException(String.format("Error aborting the process: process is in %s state", getStatus()));
    }
    if (handler!=null) {
      handler.abort();
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
