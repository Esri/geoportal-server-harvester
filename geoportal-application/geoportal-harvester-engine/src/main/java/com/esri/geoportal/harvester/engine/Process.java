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

import com.esri.geoportal.harvester.api.IProcess;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Process.
 */
public class Process implements IProcess {
  private final ReportBuilder reportBuilder;
  private final Processor processor;
  private final Map<String,String> processorEnv;
  private final Task task;
  private final ArrayList<Listener> listeners = new ArrayList<>();
  
  private Status status = Status.submitted;

  /**
   * Creates instance of the process.
   * @param reportBuilder report builder
   * @param processor processor
   * @param processorEnv processor variables
   * @param task task
   */
  public Process(ReportBuilder reportBuilder, Processor processor, Map<String,String> processorEnv, Task task) {
    this.reportBuilder = reportBuilder;
    this.processor = processor;
    this.processorEnv = processorEnv;
    this.task = task;
  }

  @Override
  public void addListener(Listener listener) {
    listeners.add(listener);
  }
  
  /**
   * Gets process title.
   * @return process title
   */
  public String getTitle() {
    return String.format("%s --> [%s]", task.getDataSource().toString(), task.getDataDestinations().stream().map(d->d.toString()).collect(Collectors.joining(",")));
  }
  
  /**
   * Gets process status.
   * @return process status
   */
  @Override
  public synchronized Status getStatus() {
    return status;
  }
  
  /**
   * Sets status.
   * @param status 
   */
  private synchronized void setStatus(Status status) {
    this.status = status;
  }
  
  /**
   * Begins the process.
   */
  @Override
  public synchronized void begin() {
    if (getStatus()!=Status.submitted) {
      throw new IllegalStateException(String.format("Error begininig the process: process is in %s state", getStatus()));
    }
    
    /*
    handler = processor.submit(task.getDataSource(), task.getDataDestinations(), new DefaultProcessor.Listener() {
      @Override
      public void onStarted() {
        reportBuilder.started(Process.this);
      }

      @Override
      public void onCompleted() {
        reportBuilder.completed(Process.this);
      }

      @Override
      public void onSuccess(DataReference dataReference) {
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
    
    handler.begin();
    */
  }
  
  /**
   * Aborts the process.
   */
  @Override
  public synchronized void abort() {
    if (getStatus()!=Status.working) {
      throw new IllegalStateException(String.format("Error aborting the process: process is in %s state", getStatus()));
    }
    /*
    if (handler!=null) {
      handler.abort();
    }
    */
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
