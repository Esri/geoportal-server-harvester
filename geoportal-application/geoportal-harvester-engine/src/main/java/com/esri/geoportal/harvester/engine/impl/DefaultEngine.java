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

import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import java.util.UUID;
import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.TriggerInstance;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.BrokersService;
import com.esri.geoportal.harvester.engine.Engine;
import com.esri.geoportal.harvester.engine.ExecutionService;
import com.esri.geoportal.harvester.engine.ProcessesService;
import com.esri.geoportal.harvester.engine.TasksService;
import com.esri.geoportal.harvester.engine.TemplatesService;
import com.esri.geoportal.harvester.engine.TriggersService;
import com.esri.geoportal.harvester.engine.support.CrudsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Harvesting engine.
 */
public class DefaultEngine implements Engine {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultEngine.class);
  
  protected final TemplatesService templatesService;
  protected final BrokersService brokersService;
  protected final TasksService tasksService;
  protected final ProcessesService processesService;
  protected final TriggersService triggersService;
  protected final ExecutionService executionService;

  /**
   * Creates instance of the engine.
   *
   * @param inboundConnectorRegistry inbound connector registry
   * @param outboundConnectorRegistry outbound connector registry
   * @param triggerRegistry trigger registry
   * @param processorRegistry processor registry
   * @param brokerDefinitionManager broker definition manager
   * @param taskManager task manager
   * @param processManager process manager
   * @param triggerManager trigger manager
   * @param triggerInstanceManager trigger instance manager
   * @param historyManager history manager
   * @param reportBuilder report builder
   */
  public DefaultEngine(
          TemplatesService templatesService,
          BrokersService brokersService,
          TasksService tasksService,
          ProcessesService processesService,
          TriggersService triggersService,
          ExecutionService executionService
  ) {
    this.templatesService = templatesService;
    this.brokersService = brokersService;
    this.tasksService = tasksService;
    this.processesService = processesService;
    this.triggersService = triggersService;
    this.executionService = executionService;
  }

  @Override
  public TemplatesService getTemplatesService() {
    return templatesService;
  }

  @Override
  public BrokersService getBrokersService() {
    return brokersService;
  }

  @Override
  public TasksService getTasksService() {
    return tasksService;
  }

  @Override
  public ProcessesService getProcessesService() {
    return processesService;
  }

  @Override
  public TriggersService getTriggersService() {
    return triggersService;
  }

  @Override
  public ExecutionService getExecutionService() {
    return executionService;
  }
  
  /**
   * Activates trigger instances
   */
  protected void activateTriggerInstances() {
    try {
      getTriggersService().select().forEach(e->{
        UUID uuid = e.getKey();
        TriggerManager.TriggerDefinitionUuidPair definition = e.getValue();
        
        try {
          Trigger trigger = getTriggersService().getTrigger(definition.triggerDefinition.getType());
          if (trigger==null) {
            throw new InvalidDefinitionException(String.format("Invalid trigger type: %s", definition.triggerDefinition.getType()));
          }
          TriggerInstance triggerInstance = trigger.createInstance(definition.triggerDefinition);
          TriggerInstance.Context context = executionService.newTriggerContext(definition.taskUuid);
          triggerInstance.activate(context);
        } catch (DataProcessorException|InvalidDefinitionException ex) {
          LOG.warn(String.format("Error creating and activating trigger instance: %s -> %s", uuid, definition), ex);
        }
      });
    } catch (CrudsException ex) {
      LOG.error("Error processing trigger definitions", ex);
    }
  }
  
  /**
   * Deactivates trigger instances.
   */
  protected void deactivateTriggerInstances() {
    getTriggersService().listAll().stream().forEach(e->{
      UUID uuid = e.getKey();
      TriggerInstance triggerInstance = e.getValue();
      
      try {
        triggerInstance.close();
      } catch (Exception ex) {
        LOG.warn(String.format("Error deactivating trigger instance: %s --> %s", uuid, triggerInstance.getTriggerDefinition()), ex);
      }
    });
    getTriggersService().clear();
  }
}
