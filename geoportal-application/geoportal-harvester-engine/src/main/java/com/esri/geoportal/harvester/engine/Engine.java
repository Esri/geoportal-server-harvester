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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.esri.geoportal.harvester.api.BrokerDefinition;
import com.esri.geoportal.harvester.api.ConnectorTemplate;
import com.esri.geoportal.harvester.api.InputBroker;
import com.esri.geoportal.harvester.api.InputConnector;
import com.esri.geoportal.harvester.api.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.OutputBroker;
import com.esri.geoportal.harvester.api.OutputConnector;

/**
 * Harvesting engine.
 */
public class Engine {
  private final ReportBuilder reportBuilder;
  private final TaskManager taskManager;
  private final ProcessManager processManager;
  private final InboundConnectorRegistry dsReg;
  private final OutboundConnectorRegistry dpReg;

  /**
   * Creates instance of the engine.
   * @param reportBuilder report builder
   * @param taskManager task manager
   * @param processManager process manager
   * @param dsReg data source registry
   * @param dpReg data publisher registry
   */
  public Engine(ReportBuilder reportBuilder, TaskManager taskManager, ProcessManager processManager, InboundConnectorRegistry dsReg, OutboundConnectorRegistry dpReg) {
    this.reportBuilder = reportBuilder;
    this.taskManager = taskManager;
    this.processManager = processManager;
    this.dsReg = dsReg;
    this.dpReg = dpReg;
  }

  /**
   * Gets inbound connector templates.
   * @return collection of inbound connector templates
   */
  public Collection<ConnectorTemplate> getInboundConnectorTemplates() {
    return dsReg.getTemplates();
  }

  /**
   * Gets outbound connector templates.
   * @return collection of outbound connector templates
   */
  public Collection<ConnectorTemplate> getOutboundConnectorTemplates() {
    return dpReg.getTemplates();
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
   * Creates task to execute.
   * @param dsParams data input init parameter
   * @param dpParams data output init parameters
   * @return task
   * @throws InvalidDefinitionException if one of broker definitions appears to be invalid
   */
  public Task<String> createTask(BrokerDefinition dsParams, List<BrokerDefinition> dpParams) throws InvalidDefinitionException {
    InputConnector<InputBroker> dsFactory = dsReg.get(dsParams.getType());
    
    if (dsFactory==null) {
      throw new IllegalArgumentException("Invalid data source init parameters");
    }
    
    InputBroker<String> dataSource = dsFactory.createBroker(dsParams);

    ArrayList<OutputBroker<String>> dataDestinations =  new ArrayList<>();
    for (BrokerDefinition def: dpParams) {
      OutputConnector<OutputBroker> dpFactory = dpReg.get(def.getType());
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
    Process process = new Process(reportBuilder, task);
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
