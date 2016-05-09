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
package com.esri.geoportal.harvester.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.esri.geoportal.harvester.api.BrokerDefinition;
import com.esri.geoportal.harvester.api.ConnectorTemplate;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import com.esri.geoportal.harvester.engine.BrokerInfo.Category;
import static com.esri.geoportal.harvester.engine.BrokerInfo.Category.INBOUND;
import static com.esri.geoportal.harvester.engine.BrokerInfo.Category.OUTBOUND;
import java.util.Set;

/**
 * Harvesting engine.
 */
public class Engine {
  private final ReportBuilder reportBuilder;
  private final TaskManager taskManager;
  private final ProcessManager processManager;
  private final TriggerManager triggerManager;
  private final InboundConnectorRegistry inboundConnectorRegistry;
  private final OutboundConnectorRegistry outboundConnectorRegistry;
  private final TriggerRegistry triggerRegistry;
  private final BrokerDefinitionManager brokerDefinitionManager;

  /**
   * Creates instance of the engine.
   * @param inboundConnectorRegistry inbound connector registry
   * @param outboundConnectorRegistry outbound connector registry
   * @param triggerRegistry trigger registry
   * @param brokerDefinitionManager broker definition manager
   * @param taskManager task manager
   * @param processManager process manager
   * @param triggerManager trigger manager
   * @param reportBuilder report builder
   */
  public Engine(
          InboundConnectorRegistry inboundConnectorRegistry, 
          OutboundConnectorRegistry outboundConnectorRegistry, 
          TriggerRegistry triggerRegistry,
          BrokerDefinitionManager brokerDefinitionManager,
          TaskManager taskManager, 
          ProcessManager processManager, 
          TriggerManager triggerManager,
          ReportBuilder reportBuilder 
  ) {
    this.inboundConnectorRegistry = inboundConnectorRegistry;
    this.outboundConnectorRegistry = outboundConnectorRegistry;
    this.triggerRegistry = triggerRegistry;
    this.taskManager = taskManager;
    this.processManager = processManager;
    this.brokerDefinitionManager = brokerDefinitionManager;
    this.triggerManager = triggerManager;
    this.reportBuilder = reportBuilder;
  }

  /**
   * Gets inbound connector templates.
   * @return collection of inbound connector templates
   */
  public Collection<ConnectorTemplate> getInboundConnectorTemplates() {
    return inboundConnectorRegistry.getTemplates();
  }

  /**
   * Gets outbound connector templates.
   * @return collection of outbound connector templates
   */
  public Collection<ConnectorTemplate> getOutboundConnectorTemplates() {
    return outboundConnectorRegistry.getTemplates();
  }

  /**
   * Gets inbound brokers definitions.
   * @return collection of inbound brokers definitions
   */
  public Collection<BrokerInfo> getInboundBrokersDefinitions() {
    Set<String> inboundTypes = inboundConnectorRegistry.getTemplates().stream().map(t->t.getType()).collect(Collectors.toSet());
    brokerDefinitionManager.select().stream().filter(e->inboundTypes.contains(e.getValue().getType()));
    return brokerDefinitionManager.select().stream()
            .filter(e->inboundTypes.contains(e.getValue().getType()))
            .map(e->new BrokerInfo(e.getKey(),INBOUND,e.getValue()))
            .collect(Collectors.toList());
  }

  /**
   * Gets outbound brokers definitions.
   * @return collection of outbound brokers definitions
   */
  public Collection<BrokerInfo> getOutboundBrokersDefinitions() {
    Set<String> outboundTypes = outboundConnectorRegistry.getTemplates().stream().map(t->t.getType()).collect(Collectors.toSet());
    return brokerDefinitionManager.select().stream()
            .filter(e->outboundTypes.contains(e.getValue().getType()))
            .map(e->new BrokerInfo(e.getKey(),OUTBOUND,e.getValue()))
            .collect(Collectors.toList());
  }
  
  private Category getBrokerCategoryByType(String brokerType) {
    Set<String> inboundTypes = inboundConnectorRegistry.getTemplates().stream().map(t->t.getType()).collect(Collectors.toSet());
    Set<String> outboundTypes = outboundConnectorRegistry.getTemplates().stream().map(t->t.getType()).collect(Collectors.toSet());
    return inboundTypes.contains(brokerType)? INBOUND: outboundTypes.contains(brokerType)? OUTBOUND: null;
  }
  
  /**
   * Finds broker by id.
   * @param brokerId broker id
   * @return brokrt or <code>null</code> if no broker corresponding to the broker id can be found
   */
  public BrokerInfo findBroker(UUID brokerId) {
    BrokerDefinition brokerDefinition = brokerDefinitionManager.read(brokerId);
    if (brokerDefinition!=null) {
      Category category = getBrokerCategoryByType(brokerDefinition.getType());
      if (category!=null) {
        return new BrokerInfo(brokerId, category, brokerDefinition);
      }
    }
    return null;
  }
  
  /**
   * Creates a broker.
   * @param brokerDefinition broker definition
   * @return broker info or <code>null</code> if broker has not been created
   */
  public BrokerInfo createBroker(BrokerDefinition brokerDefinition) {
    Category category = getBrokerCategoryByType(brokerDefinition.getType());
    if (category!=null) {
      UUID id = brokerDefinitionManager.create(brokerDefinition);
      return new BrokerInfo(id, category, brokerDefinition);
    }
    return null;
  }
  
  /**
   * Creates a broker.
   * @param brokerId broker id
   * @param brokerDefinition broker definition
   * @return broker info or <code>null</code> if broker has not been created
   */
  public BrokerInfo updateBroker(UUID brokerId, BrokerDefinition brokerDefinition) {
    BrokerDefinition oldBrokerDef = brokerDefinitionManager.read(brokerId);
    if (oldBrokerDef!=null) {
      if (!brokerDefinitionManager.update(brokerId, brokerDefinition)) {
        oldBrokerDef = null;
      }
    }
    Category category = oldBrokerDef!=null? getBrokerCategoryByType(oldBrokerDef.getType()): null;
    return category!=null? new BrokerInfo(brokerId, category, brokerDefinition): null;
  }
  
  /**
   * Deletes broker.
   * @param brokerId broker id
   * @return <code>true</code> if broker has been deleted
   */
  public boolean deleteBroker(UUID brokerId) {
    return brokerDefinitionManager.delete(brokerId);
  }

  /**
   * Gets process by process id.
   * @param processId process id.
   * @return process or <code>null</code> if no process available for the given process id
   */
  public Process getProcess(UUID processId) {
    return processManager.read(processId);
  }
  
  /**
   * Selects processes by predicate.
   * @param predicate predicate
   * @return list of processes matching predicate
   */
  public List<Map.Entry<UUID,Process>> selectProcesses(Predicate<? super Map.Entry<UUID, Process>> predicate) {
    return processManager.select().stream().filter(predicate!=null? predicate: (Map.Entry<UUID, Process> e) -> true).collect(Collectors.toList());
  }
  
  /**
   * Creates task to initialize.
   * @param dsParams data input init parameter
   * @param dpParams data output init parameters
   * @return task
   * @throws InvalidDefinitionException if one of broker definitions appears to be invalid
   */
  public Task<String> createTask(BrokerDefinition dsParams, List<BrokerDefinition> dpParams) throws InvalidDefinitionException {
    InputConnector<InputBroker> dsFactory = inboundConnectorRegistry.get(dsParams.getType());
    
    if (dsFactory==null) {
      throw new IllegalArgumentException("Invalid data source init parameters");
    }
    
    InputBroker<String> dataSource = dsFactory.createBroker(dsParams);

    ArrayList<OutputBroker<String>> dataDestinations =  new ArrayList<>();
    for (BrokerDefinition def: dpParams) {
      OutputConnector<OutputBroker> dpFactory = outboundConnectorRegistry.get(def.getType());
      if (dpFactory==null) {
        throw new IllegalArgumentException("Invalid data publisher init parameters");
      }

      OutputBroker<String> dataPublisher = dpFactory.createBroker(def);
      dataDestinations.add(dataPublisher);
    }
    
    return new Task(dataSource, dataDestinations);
  }
  
  /**
   * Creates process.
   * @param task task for the process
   * @return process
   */
  public UUID createProcess(Task task) {
    DefaultProcessor processor = new DefaultProcessor();
    Process process = new Process(reportBuilder, processor, task);
    return processManager.create(process);
  }
  
  /**
   * Selects task definitions.
   * @param predicate predicate
   * @return list of task definitions matching predicate
   */
  public List<Map.Entry<UUID,TaskDefinition>> selectTaskDefinitions(Predicate<? super Map.Entry<UUID, TaskDefinition>> predicate) {
    return taskManager.select().stream().filter(predicate!=null? predicate: (Map.Entry<UUID, TaskDefinition> e) -> true).collect(Collectors.toList());
  }
  
  /**
   * Reads task definition.
   * @param taskId task id
   * @return task definition
   */
  public TaskDefinition readTaskDefinition(UUID taskId) {
    return taskManager.read(taskId);
  }
  
  /**
   * Deletes task definition.
   * @param taskId task id
   * @return <code>true</code> if task definition has been deleted
   */
  public boolean deleteTaskDefinition(UUID taskId) {
    return taskManager.delete(taskId);
  }
  
  /**
   * Adds task definition.
   * @param taskDefinition task definition
   * @return id of a new task
   */
  public UUID addTaskDefinition(TaskDefinition taskDefinition) {
    return taskManager.create(taskDefinition);
  }
  
  /**
   * Updates task.
   * @param taskId task id
   * @param taskDefinition task definition
   * @return old task definition or <code>null</code> if no old task
   */
  public TaskDefinition updateTaskDefinition(UUID taskId, TaskDefinition taskDefinition) {
    TaskDefinition oldTaskDef = taskManager.read(taskId);
    if (oldTaskDef!=null) {
      if (!taskManager.update(taskId, taskDefinition)) {
        oldTaskDef = null;
      }
    }
    return oldTaskDef;
  }
}
