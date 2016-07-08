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

/**
 * Engine interface.
 */
public interface Engine {
  /**
   * Gets templates service.
   * @return templates service
   */
  TemplatesService getTemplatesService();
  
  /**
   * Gets brokers service.
   * @return brokers service
   */
  BrokersService getBrokersService();
  
  /**
   * Gets tasks service.
   * @return tasks service
   */
  TasksService getTasksService();
  
  /**
   * Gets processes service.
   * @return processes service
   */
  ProcessesService getProcessesService();
  
  /**
   * Gets triggers service.
   * @return triggers service
   */
  TriggersService getTriggersService();
  
  /**
   * Gets execution service.
   * @return execution service
   */
  ExecutionService getExecutionService();
}
