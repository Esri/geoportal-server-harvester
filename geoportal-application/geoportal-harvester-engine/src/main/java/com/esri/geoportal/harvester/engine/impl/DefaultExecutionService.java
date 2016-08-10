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

import com.esri.geoportal.harvester.api.Filter;
import com.esri.geoportal.harvester.api.FilterInstance;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.Transformer;
import com.esri.geoportal.harvester.api.TransformerInstance;
import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.TriggerInstance;
import com.esri.geoportal.harvester.api.base.BrokerLinkActionAdaptor;
import com.esri.geoportal.harvester.api.base.FilterLinkActionAdaptor;
import com.esri.geoportal.harvester.api.base.SimpleLink;
import com.esri.geoportal.harvester.api.base.TransformerLinkActionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.LinkDefinition;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.general.Link;
import com.esri.geoportal.harvester.api.general.LinkAction;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import com.esri.geoportal.harvester.engine.ExecutionService;
import com.esri.geoportal.harvester.engine.ProcessesService;
import com.esri.geoportal.harvester.engine.registers.FilterRegistry;
import com.esri.geoportal.harvester.engine.managers.History;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.registers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.registers.TransformerRegistry;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager.TaskUuidTriggerInstancePair;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.registers.TriggerRegistry;
import com.esri.geoportal.harvester.engine.support.CrudlException;
import com.esri.geoportal.harvester.engine.support.HistoryManagerAdaptor;
import com.esri.geoportal.harvester.engine.support.ProcessReference;
import com.esri.geoportal.harvester.engine.support.TriggerReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default execution service.
 */
public class DefaultExecutionService implements ExecutionService {
  protected final InboundConnectorRegistry inboundConnectorRegistry;
  protected final OutboundConnectorRegistry outboundConnectorRegistry;
  protected final TransformerRegistry transformerRegistry;
  protected final FilterRegistry filterRegistry;
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
   * @param transformerRegistry transformer registry
   * @param filterRegistry filter registry
   * @param processorRegistry processor registry
   * @param triggerRegistry trigger registry
   * @param triggerManager trigger manager
   * @param triggerInstanceManager trigger instance manager
   * @param historyManager history manager
   * @param processesService processes service
   */
  public DefaultExecutionService(
          InboundConnectorRegistry inboundConnectorRegistry, 
          OutboundConnectorRegistry outboundConnectorRegistry, 
          TransformerRegistry transformerRegistry,
          FilterRegistry filterRegistry,
          ProcessorRegistry processorRegistry, 
          TriggerRegistry triggerRegistry, 
          TriggerManager triggerManager, 
          TriggerInstanceManager triggerInstanceManager, 
          HistoryManager historyManager, 
          ProcessesService processesService) {
    this.inboundConnectorRegistry = inboundConnectorRegistry;
    this.outboundConnectorRegistry = outboundConnectorRegistry;
    this.transformerRegistry = transformerRegistry;
    this.filterRegistry = filterRegistry;
    this.processorRegistry = processorRegistry;
    this.triggerRegistry = triggerRegistry;
    this.triggerManager = triggerManager;
    this.triggerInstanceManager = triggerInstanceManager;
    this.historyManager = historyManager;
    this.processesService = processesService;
  }

  @Override
  public ProcessReference execute(TaskDefinition taskDefinition, Map<String,Object> attributes) throws InvalidDefinitionException, DataProcessorException {
    Task task = createTask(taskDefinition);
    return processesService.createProcess(task, attributes);
  }
  
  @Override
  public TriggerReference schedule(UUID taskId, TriggerDefinition trigDef, Map<String,Object> attributes) throws InvalidDefinitionException, DataProcessorException {
    try {
      TriggerManager.TaskUuidTriggerDefinitionPair pair = new TriggerManager.TaskUuidTriggerDefinitionPair();
      pair.setTaskUuid(taskId);
      pair.setTriggerDefinition(trigDef);
      UUID uuid = triggerManager.create(pair);
      Trigger trigger = triggerRegistry.get(trigDef.getType());
      TriggerInstance triggerInstance = trigger.createInstance(trigDef);
      TaskUuidTriggerInstancePair pair2 = new TriggerInstanceManager.TaskUuidTriggerInstancePair();
      pair2.setTaskId(taskId);
      pair2.setTriggerInstance(triggerInstance);
      triggerInstanceManager.put(uuid, pair2);
      TriggerContext context = new TriggerContext(taskId);
      triggerInstance.activate(context);
      return new TriggerReference(uuid, taskId, trigDef);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error scheduling task: %s", trigDef.getTaskDefinition()), ex);
    }
  }

  @Override
  public TriggerInstance.Context newTriggerContext(UUID taskId) {
    return new TriggerContext(taskId);
  }
  
  /**
   * Creates new task.
   * @param taskDefinition task definition
   * @return task
   * @throws InvalidDefinitionException  if invalid definition
   */
  private Task createTask(TaskDefinition taskDefinition) throws InvalidDefinitionException {
    InputBroker dataSource = newInputBroker(taskDefinition.getSource());

    ArrayList<Link> dataDestinations = new ArrayList<>();
    for (LinkDefinition def : taskDefinition.getDestinations()) {
      dataDestinations.add(newLink(def));
    }
    
    Processor processor = newProcessor(taskDefinition.getProcessor());
    
    return new Task(processor, dataSource, dataDestinations);
  }
  
  /**
   * Creates new processor.
   * @param processorDefinition processor definition
   * @return processor
   * @throws InvalidDefinitionException if invalid definition
   */
  private Processor newProcessor(EntityDefinition processorDefinition) throws InvalidDefinitionException {
    Processor processor = processorDefinition == null
            ? processorRegistry.getDefaultProcessor()
            : processorRegistry.get(processorDefinition.getType()) != null
            ? processorRegistry.get(processorDefinition.getType())
            : null;
    if (processor == null) {
      throw new InvalidDefinitionException(String.format("Unable to select processor based on definition: %s", processorDefinition));
    }
    return processor;
  }
  
  /**
   * Creates new input broker.
   * @param entityDefinition input broker definition
   * @return input broker
   * @throws InvalidDefinitionException if invalid definition
   */
  private InputBroker newInputBroker(EntityDefinition entityDefinition) throws InvalidDefinitionException {
    InputConnector<InputBroker> dsFactory = inboundConnectorRegistry.get(entityDefinition.getType());

    if (dsFactory == null) {
      throw new InvalidDefinitionException("Invalid input broker definition");
    }

    return dsFactory.createBroker(entityDefinition);
  }

  /**
   * Creates new output broker.
   * @param entityDefinition output broker definition
   * @return output broker
   * @throws InvalidDefinitionException if invalid definition
   */  
  private OutputBroker newOutputBroker(EntityDefinition entityDefinition) throws InvalidDefinitionException {
    OutputConnector<OutputBroker> dpFactory = outboundConnectorRegistry.get(entityDefinition.getType());

    if (dpFactory == null) {
      throw new IllegalArgumentException("Invalid output broker definition");
    }

    return dpFactory.createBroker(entityDefinition);
  }

  /**
   * Creates new link.
   * @param linkDefinition link definition
   * @return link
   * @throws InvalidDefinitionException if invalid definition
   */
  private Link newLink(LinkDefinition linkDefinition) throws InvalidDefinitionException {
    LinkAction linkAction = newLinkAction(linkDefinition.getAction());
    ArrayList<Link> drains = new ArrayList<>();
    if (linkDefinition.getDrains()!=null) {
      for (LinkDefinition drainDef: linkDefinition.getDrains()) {
        drains.add(newLink(drainDef));
      }
    }
    return new SimpleLink(linkAction, drains);
  }
  
  /**
   * Creates new link action.
   * @param actionDefinition action definition
   * @return link action
   * @throws InvalidDefinitionException if invalid definition.
   */
  private LinkAction newLinkAction(EntityDefinition actionDefinition) throws InvalidDefinitionException {
    OutputConnector<OutputBroker> outputConnector = outboundConnectorRegistry.get(actionDefinition.getType());
    if (outputConnector!=null) {
      OutputBroker broker = outputConnector.createBroker(actionDefinition);
      return new BrokerLinkActionAdaptor(broker);
    }
    
    Filter filter = filterRegistry.get(actionDefinition.getType());
    if (filter!=null) {
      FilterInstance filterInstance = filter.createInstance(actionDefinition);
      return new FilterLinkActionAdaptor(filterInstance);
    }
    
    Transformer transformer = transformerRegistry.get(actionDefinition.getType());
    if (transformer!=null) {
      TransformerInstance transformerInstance = transformer.createInstance(actionDefinition);
      return new TransformerLinkActionAdaptor(transformerInstance);
    }
    
    throw new InvalidDefinitionException(String.format("Error creating link action for: %s", actionDefinition.getType()));
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
    public synchronized ProcessInstance execute(TaskDefinition taskDefinition) throws DataProcessorException, InvalidDefinitionException {
      HashMap<String,Object> attributes = new HashMap<>();
      Date lastHarvest = lastHarvest();
      if (lastHarvest!=null) {
        attributes.put("Last-Harvested", lastHarvest);
      }
      ProcessReference ref = DefaultExecutionService.this.execute(taskDefinition,attributes);
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
          History.Event lastEvent = history!=null? history.stream()
                  .sorted((left,right)->0-left.getStartTimestamp().compareTo(right.getStartTimestamp()))
                  .findFirst()
                  .orElse(null): null;
          return lastEvent!=null? lastEvent.getStartTimestamp(): null;
        } else {
          return null;
        }
      } catch (CrudlException ex) {
        throw new DataProcessorException(String.format("Error getting last harvest for: %s", taskId), ex);
      }
    }
  }
}
