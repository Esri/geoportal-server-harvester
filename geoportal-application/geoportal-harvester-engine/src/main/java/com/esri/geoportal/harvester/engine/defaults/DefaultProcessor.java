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
package com.esri.geoportal.harvester.engine.defaults;

import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Initializable.InitContext;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.base.SimpleInitContext;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.general.Link;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputBroker.IteratorContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultProcessor.
 */
public class DefaultProcessor implements Processor {

  public static final String TYPE = "DEFAULT";

  private static final Logger LOG = LoggerFactory.getLogger(DefaultProcessor.class);
  
  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    UITemplate uiTemplate = new UITemplate(getType(), "Default processor", null);
    return uiTemplate;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    EntityDefinition entityDefiniton = new EntityDefinition();
    entityDefiniton.setType(TYPE);
    entityDefiniton.setLabel(TYPE);
    return entityDefiniton;
  }

  @Override
  public ProcessInstance createProcess(Task task, IteratorContext iteratorContext) {
    LOG.info(String.format("SUBMITTING: %s", task));
    return new DefaultProcess(task, iteratorContext);
  }

  /**
   * Default process.
   */
  public static class DefaultProcess implements ProcessInstance {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProcess.class);
    private final List<ProcessInstance.Listener> listeners = Collections.synchronizedList(new ArrayList<>());

    final Task task;
    final Thread thread;

    private volatile boolean completed;
    private volatile boolean aborting;

    private String getTitle() {
      return task.getTaskDefinition().toString();
    }
    
    /**
     * Initializes all elements.
     * @param context init context
     * @throws DataProcessorException if initialization fails
     */
    private void initialize(InitContext context) throws DataProcessorException {
      task.getDataSource().initialize(context);
      for (Link link: task.getDataDestinations()) {
        link.initialize(context);
      }
    }
    
    /**
     * Terminates all tasks
     */
    private void terminate() {
      if (!Thread.currentThread().isInterrupted()) {
        task.getDataDestinations().stream().forEach(Link::terminate);
        task.getDataSource().terminate();
      }
    }
    
    /**
     * Creates instance of the process.
     *
     * @param task task
     * @param iteratorContext iteration context
     */
    public DefaultProcess(Task task, IteratorContext iteratorContext) {
      this.task = task;
      this.thread = new Thread(() -> {
        InitContext initContext = new SimpleInitContext(task,listeners);
        LOG.info(formatForLog("Started harvest: %s", getTitle()));
        
        if (!task.getDataDestinations().isEmpty()) {
          try {
            initialize(initContext);
            onStatusChange();
            
            InputBroker.Iterator iterator = task.getDataSource().iterator(iteratorContext);
            while (iterator.hasNext()) {
              if (Thread.currentThread().isInterrupted()) {
                break;
              }
              DataReference dataReference = iterator.next();
              onAcquire(dataReference);
              task.getDataDestinations().stream().forEach((d) -> {
                try {
                  PublishingStatus status = d.push(dataReference);
                  LOG.debug(formatForLog("Harvested %s during %s", dataReference, getTitle()));
                  onSuccess(dataReference, status);
                } catch (DataProcessorException ex) {
                  LOG.warn(formatForLog("Failed harvesting %s during %s", dataReference, getTitle()));
                  onError(ex);
                } catch (DataOutputException ex) {
                  LOG.warn(formatForLog("Failed harvesting %s during %s", dataReference, getTitle()));
                  onError(ex);
                }
              });
            }
            
          } catch (DataInputException ex) {
            LOG.error(formatForLog("Error harvesting of %s", getTitle()), ex);
            onError(ex);
          } catch (DataProcessorException ex) {
            LOG.error(formatForLog("Error harvesting of %s", getTitle()), ex);
            onError(ex);
          } finally {
            terminate();
            completed = true;
            aborting = false;
            Thread.interrupted();
            onStatusChange();
          }
        }
      }, "HARVESTING");
    }

    @Override
    public Task getTask() {
      return task;
    }

    @Override
    public void addListener(ProcessInstance.Listener listener) {
      listeners.add(listener);
    }

    /**
     * Gets process status.
     *
     * @return process status
     */
    @Override
    public synchronized ProcessInstance.Status getStatus() {
      if (completed) {
        return ProcessInstance.Status.completed;
      }
      if (aborting) {
        return ProcessInstance.Status.aborting;
      }
      if (thread.isAlive()) {
        return ProcessInstance.Status.working;
      }
      return ProcessInstance.Status.submitted;
    }

    @Override
    public void init() {
      onStatusChange();
    }

    /**
     * Begins the process.
     */
    @Override
    public synchronized void begin() {
      if (getStatus() != ProcessInstance.Status.submitted) {
        throw new IllegalStateException(formatForLog("Error begininig the process: process is in %s state", getStatus()));
      }
      thread.start();
    }

    /**
     * Aborts the process.
     */
    @Override
    public synchronized void abort() {
      if (getStatus() != ProcessInstance.Status.working) {
        throw new IllegalStateException(formatForLog("Error aborting the process: process is in %s state", getStatus()));
      }
      LOG.info(String.format("Aborting process: %s", getTitle()));
      aborting = true;
      onStatusChange();
      thread.interrupt();
    }

    /**
     * Called to handle output error.
     *
     * @param ex output exception
     */
    private void onError(DataOutputException ex) {
      listeners.forEach(l -> {
        l.onError(ex);
      });
    }

    /**
     * Called to handle processor error.
     *
     * @param ex processor exception
     */
    private void onError(DataProcessorException ex) {
      listeners.forEach(l -> {
        l.onError(ex);
      });
    }

    /**
     * Called to handle input error.
     *
     * @param ex input exception
     */
    private void onError(DataInputException ex) {
      listeners.forEach(l -> l.onError(ex));
    }

    /**
     * Called to handle successful data processing
     *
     * @param dataRef data reference
     * @param status publishing status
     */
    private void onSuccess(DataReference dataRef, PublishingStatus status) {
      listeners.forEach(l -> l.onDataProcessed(dataRef, status));
    }

    /**
     * Called to handle successful data acquiring
     *
     * @param dataRef data reference
     */
    private void onAcquire(DataReference dataRef) {
      listeners.forEach(l -> l.onDataAcquired(dataRef));
    }

    /**
     * Called when status has been changed.
     */
    private void onStatusChange() {
      Status status = getStatus();
      listeners.forEach(l -> l.onStatusChange(status));
    }

    @Override
    public String toString() {
      return String.format("PROCESS:: status: %s, title: %s", getStatus(), getTitle());
    }
  }
}
