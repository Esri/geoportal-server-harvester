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

import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker.IteratorContext;
import com.esri.geoportal.harvester.engine.services.ExecutionService;
import com.esri.geoportal.harvester.engine.services.ProcessesService;
import com.esri.geoportal.harvester.engine.services.TasksService;
import com.esri.geoportal.harvester.engine.utils.ProcessReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Default execution service.
 */
public class DefaultExecutionService implements ExecutionService {
  protected final ProcessesService processesService;
  protected final TasksService tasksService;

  /**
   * Creates instance of the service.
   * @param processesService processes service
   * @param tasksService tasks service
   */
  public DefaultExecutionService(
          ProcessesService processesService,
          TasksService tasksService
  ) {
    this.processesService = processesService;
    this.tasksService = tasksService;
  }

  @Override
  public ProcessReference execute(TaskDefinition taskDefinition, IteratorContext iteratorContext) throws InvalidDefinitionException, DataProcessorException,TimeoutException,ExecutionException,InterruptedException {
    Task task = tasksService.createTask(taskDefinition);
    return processesService.createProcess(task, iteratorContext);
  }
}
