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
package com.esri.geoportal.harvester.engine.services;

import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.managers.History;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * Tasks service.
 */
public interface TasksService {

  /**
   * Adds task definition.
   *
   * @param taskDefinition task definition
   * @return id of a new task
   * @throws DataProcessorException if accessing repository fails
   */
  UUID addTaskDefinition(TaskDefinition taskDefinition) throws DataProcessorException;

  /**
   * Updates task.
   *
   * @param taskId task id
   * @param taskDefinition task definition
   * @return old task definition or <code>null</code> if no old task
   * @throws DataProcessorException if accessing repository fails
   */
  TaskDefinition updateTaskDefinition(UUID taskId, TaskDefinition taskDefinition) throws DataProcessorException;

  /**
   * Reads task definition.
   *
   * @param taskId task id
   * @return task definition
   * @throws DataProcessorException if accessing repository fails
   */
  TaskDefinition readTaskDefinition(UUID taskId) throws DataProcessorException;

  /**
   * Deletes task definition.
   *
   * @param taskId task id
   * @return <code>true</code> if task definition has been deleted
   * @throws DataProcessorException if accessing repository fails
   */
  boolean deleteTaskDefinition(UUID taskId) throws DataProcessorException;

  /**
   * Selects task definitions.
   *
   * @param predicate predicate
   * @return list of task definitions matching predicate
   * @throws DataProcessorException if accessing repository fails
   */
  List<Map.Entry<UUID, TaskDefinition>> selectTaskDefinitions(Predicate<? super Map.Entry<UUID, TaskDefinition>> predicate) throws DataProcessorException;
  
  /**
   * Gets history.
   * @param taskId task id
   * @return list of events.
   * @throws DataProcessorException if accessing repository fails
   */
  History getHistory(UUID taskId) throws DataProcessorException;
  
  /**
   * Gets failed documents.
   * @param eventId event id
   * @return list of failed documents id's
   * @throws DataProcessorException if accessing repository fails
   */
  List<String> getFailedDocuments(UUID eventId) throws DataProcessorException;
  
  /**
   * Purges history for a given task.
   * @param taskId task id
   * @throws DataProcessorException if accessing repository fails
   */
  void purgeHistory(UUID taskId) throws DataProcessorException;
  
  /**
   * Creates task.
   * @param taskDefinition task definition
   * @return task
   * @throws InvalidDefinitionException if invalid task definition
   */
  Task createTask(TaskDefinition taskDefinition) throws InvalidDefinitionException,TimeoutException,ExecutionException,InterruptedException;
  
  /**
   * Fetching content.
   * @param taskId task id
   * @param recordId record id
   * @param credentials credentials (optional)
   * @return content or <code>null</code> if no content
   * @throws DataInputException if error fetching content
   */
  DataContent fetchContent(UUID taskId, String recordId, SimpleCredentials credentials) throws DataInputException,TimeoutException,ExecutionException,InterruptedException;
  
  /**
   * Updates all tasks with new broker definition.
   * @param brokerDefinition broker definition
   * @throws DataProcessorException if update fails
   */
  void updateTaskDefinitions(EntityDefinition brokerDefinition) throws DataProcessorException;
}
