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
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esri.geoportal.harvester.api.Process;
import java.util.Collections;

/**
 * DefaultProcess.
 */
public class DefaultProcess implements Process {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultProcess.class);
  private final List<Listener> listeners = Collections.synchronizedList(new ArrayList<>());
  private final InputBroker source;
  private final List<OutputBroker> destinations;
  
  private Status status = Status.submitted;
  private Thread thread;

  /**
   * Creates instance of the process.
   * @param source source of data
   * @param destinations data destinations
   */
  public DefaultProcess(InputBroker source, List<OutputBroker> destinations) {
    this.source = source;
    this.destinations = destinations;
    this.thread = new Thread(new Runnable() {
      @Override
      public void run() {
        LOG.info(String.format("Started harvest: %s", getTitle()));
        setStatus(Status.working);
        try {
          if (!destinations.isEmpty()) {
            while(source.hasNext()) {
              if (Thread.currentThread().isInterrupted()) break;
              DataReference dataReference = source.next();
              for (OutputBroker d: destinations) {
                try {
                  d.publish(dataReference);
                  LOG.debug(String.format("Harvested %s during %s", dataReference, getTitle()));
                  onSuccess(dataReference);
                } catch (DataOutputException ex) {
                  LOG.debug(String.format("Failed harvesting %s during %s", dataReference, getTitle()));
                  onError(ex);
                }
              }
            }
          }
        } catch (DataInputException ex) {
          onError(ex);
        } finally {
          setStatus(Status.completed);
          LOG.info(String.format("Completed harvest: %s", getTitle()));
        }
      }
    },"HARVESTING");
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
    return String.format("%s --> [%s]", source.toString(), destinations.stream().map(d->d.toString()).collect(Collectors.joining(",")));
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
    Status oldStatus = this.status;
    this.status = status;
    if (oldStatus!=status) {
      listeners.forEach(l -> {l.onStatus(status);});
    }
  }
  
  private void onError(DataOutputException ex) {
    listeners.forEach(l->{l.onError(ex);});
  }
  
  private void onError(DataInputException ex) {
    listeners.forEach(l->{l.onError(ex);});
  }
  
  private void onSuccess(DataReference dataRef) {
    listeners.forEach(l->{l.onSuccess(dataRef);});
  }
  
  /**
   * Begins the process.
   */
  @Override
  public synchronized void begin() {
    if (getStatus()!=Status.submitted) {
      throw new IllegalStateException(String.format("Error begininig the process: process is in %s state", getStatus()));
    }
    thread.start();
  }
  
  /**
   * Aborts the process.
   */
  @Override
  public synchronized void abort() {
    if (getStatus()!=Status.working) {
      throw new IllegalStateException(String.format("Error aborting the process: process is in %s state", getStatus()));
    }
    thread.interrupt();
  }
  
  @Override
  public String toString() {
    return String.format("PROCESS:: status: %s, title: %s", getStatus(), getTitle());
  }
}
