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
package com.esri.geoportal.harvester.engine.impl;

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.TriggerInstance;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import com.esri.geoportal.harvester.engine.ExecutionService;
import com.esri.geoportal.harvester.engine.ProcessesService;
import com.esri.geoportal.harvester.engine.managers.History;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.managers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.managers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.managers.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.managers.TriggerRegistry;
import com.esri.geoportal.harvester.engine.support.CrudsException;
import com.esri.geoportal.harvester.engine.support.HistoryManagerAdaptor;
import com.esri.geoportal.harvester.engine.support.ProcessReference;
import com.esri.geoportal.harvester.engine.support.TriggerReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Default execution service.
 */
public class DefaultExecutionService implements ExecutionService {
  protected final InboundConnectorRegistry inboundConnectorRegistry;
  protected final OutboundConnectorRegistry outboundConnectorRegistry;
  protected final ProcessorRegistry processorRegistry;
  protected final TriggerRegistry triggerRegistry;
  protected final TriggerManager triggerManager;
  protected final TriggerInstanceManager triggerInstanceManager;
  protected final HistoryManager historyManager;
  protected final ProcessesService processesService;

  /**
   * Creates instance of the service.
   * @param inboundConnectorRegistry inbound connector registry.
   * @param outboundConnectorRegistry outbound connector registry
   * @param processorRegistry processor registry
   * @param triggerRegistry trigger registry
   * @param triggerManager trigger manager
   * @param triggerInstanceManager trigger instance manager
   * @param historyManager history manager
   * @param processesService processes service
   */
  public DefaultExecutionService(InboundConnectorRegistry inboundConnectorRegistry, OutboundConnectorRegistry outboundConnectorRegistry, ProcessorRegistry processorRegistry, TriggerRegistry triggerRegistry, TriggerManager triggerManager, TriggerInstanceManager triggerInstanceManager, HistoryManager historyManager, ProcessesService processesService) {
    this.inboundConnectorRegistry = inboundConnectorRegistry;
    this.outboundConnectorRegistry = outboundConnectorRegistry;
    this.processorRegistry = processorRegistry;
    this.triggerRegistry = triggerRegistry;
    this.triggerManager = triggerManager;
    this.triggerInstanceManager = triggerInstanceManager;
    this.historyManager = historyManager;
    this.processesService = processesService;
  }

  @Override
  public ProcessReference submitTaskDefinition(TaskDefinition taskDefinition) throws InvalidDefinitionException, DataProcessorException {
    Task task = createTask(taskDefinition);
    return processesService.createProcess(task);
  }
  
  @Override
  public TriggerReference scheduleTask(UUID taskId, TriggerDefinition trigDef) throws InvalidDefinitionException, DataProcessorException {
    try {
      TriggerManager.TriggerDefinitionUuidPair pair = new TriggerManager.TriggerDefinitionUuidPair();
      pair.taskUuid = taskId;
      pair.triggerDefinition = trigDef;
      UUID uuid = triggerManager.create(pair);
      Trigger trigger = triggerRegistry.get(trigDef.getType());
      TriggerInstance triggerInstance = trigger.createInstance(trigDef);
      triggerInstanceManager.put(uuid, triggerInstance);
      TriggerContext context = new TriggerContext(taskId);
      triggerInstance.activate(context);
      return new TriggerReference(uuid, trigDef);
    } catch (CrudsException ex) {
      throw new DataProcessorException(String.format("Error scheduling task: %s", trigDef.getTaskDefinition()), ex);
    }
  }
  
  private Task createTask(TaskDefinition taskDefinition) throws InvalidDefinitionException {
    InputConnector<InputBroker> dsFactory = inboundConnectorRegistry.get(taskDefinition.getSource().getType());

    if (dsFactory == null) {
      throw new IllegalArgumentException("Invalid data source init parameters");
    }

    InputBroker dataSource = dsFactory.createBroker(taskDefinition.getSource());

    ArrayList<OutputBroker> dataDestinations = new ArrayList<>();
    for (EntityDefinition def : taskDefinition.getDestinations()) {
      OutputConnector<OutputBroker> dpFactory = outboundConnectorRegistry.get(def.getType());
      if (dpFactory == null) {
        throw new IllegalArgumentException("Invalid data publisher init parameters");
      }

      OutputBroker dataPublisher = dpFactory.createBroker(def);
      dataDestinations.add(dataPublisher);
    }
    
    EntityDefinition processorDefinition = taskDefinition.getProcessor();
    Processor processor = processorDefinition == null
            ? processorRegistry.getDefaultProcessor()
            : processorRegistry.get(processorDefinition.getType()) != null
            ? processorRegistry.get(processorDefinition.getType())
            : null;
    if (processor == null) {
      throw new InvalidDefinitionException(String.format("Unable to select processor based on definition: %s", processorDefinition));
    }

    return new Task(processor, dataSource, dataDestinations);
  }

  @Override
  public TriggerInstance.Context newTriggerContext(UUID taskId) {
    return new TriggerContext(taskId);
  }

  /**
   * DefaultEngine-bound trigger context.
   */
  private class TriggerContext implements TriggerInstance.Context {
    private final UUID taskId;
    
    /**
     * Creates instance of the context.
     * @param taskId task id
     */
    public TriggerContext(UUID taskId) {
      this.taskId = taskId;
    }

    @Override
    public synchronized ProcessInstance submit(TaskDefinition taskDefinition) throws DataProcessorException, InvalidDefinitionException {
      ProcessReference ref = submitTaskDefinition(taskDefinition);
      if (taskId!=null) {
        ref.getProcess().addListener(new HistoryManagerAdaptor(taskId, ref.getProcess(), historyManager));
      }
      ref.getProcess().init();
      return ref.getProcess();
    }
    
    @Override
    public Date lastHarvest() throws DataProcessorException {
      try {
        if (taskId!=null) {
          History history = historyManager.buildHistory(taskId);
          History.Event lastEvent = history.lastEvent();
          return lastEvent!=null? lastEvent.getTimestamp(): null;
        } else {
          return null;
        }
      } catch (CrudsException ex) {
        throw new DataProcessorException(String.format("Error getting last harvest for: %s", taskId), ex);
      }
    }
  }
}
