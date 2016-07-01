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
   * @param templatesService templates service
   * @param brokersService brokers service
   * @param tasksService tasks service
   * @param processesService processes service
   * @param triggersService triggers service
   * @param executionService execution service
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
}
