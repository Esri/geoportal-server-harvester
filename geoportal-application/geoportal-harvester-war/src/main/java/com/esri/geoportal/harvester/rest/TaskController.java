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
package com.esri.geoportal.harvester.rest;

import com.esri.geoportal.harvester.support.TaskInfo;
import com.esri.geoportal.harvester.beans.EngineBean;
import com.esri.geoportal.harvester.engine.TaskDefinition;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Task controller.
 */
@RestController
public class TaskController {
  private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);
  
  @Autowired
  private EngineBean engine;
  
  /**
   * Lists all available tasks.
   * @return array of task informations
   */
  @RequestMapping(value = "/rest/harvester/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public TaskInfo[] listTasks() {
    LOG.debug(String.format("GET /rest/harvester/tasks"));
    return engine.selectTaskDefinitions(null).stream().map(d->new TaskInfo(d.getKey(), d.getValue())).collect(Collectors.toList()).toArray(new TaskInfo[0]);
  }
  
  /**
   * Gets task by id.
   * @param taskId task id
   * @return task info or <code>null</code> if no task found
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public TaskInfo getTask(@PathVariable UUID taskId) {
    LOG.debug(String.format("GET /rest/harvester/tasks/%s", taskId));
    TaskDefinition taskDefinition = engine.readTaskDefinition(taskId);
    return taskDefinition!=null? new TaskInfo(taskId, taskDefinition): null;
  }
  
  /**
   * Deletes task by id.
   * @param taskId task id
   * @return task info of the deleted task or <code>null</code> if no tasks have been deleted
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public TaskInfo deleteTask(@PathVariable UUID taskId) {
    LOG.debug(String.format("DELETE /rest/harvester/tasks/%s", taskId));
    TaskDefinition taskDefinition = engine.readTaskDefinition(taskId);
    if (taskDefinition!=null) {
      engine.deleteTaskDefinition(taskId);
    }
    return taskDefinition!=null? new TaskInfo(taskId, taskDefinition): null;
  }
  
  /**
   * Adds a new task.
   * @param taskDefinition task definition
   * @return task info of the newly created task
   */
  @RequestMapping(value = "/rest/harvester/tasks", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  public TaskInfo addTask(@RequestBody TaskDefinition taskDefinition) {
    LOG.debug(String.format("PUT /rest/harvester/tasks <-- %s", taskDefinition));
    UUID id = engine.addTaskDefinition(taskDefinition);
    return new TaskInfo(id, taskDefinition);
  }
  
  
  /**
   * Updates task by id.
   * @param taskDefinition task definition
   * @param taskId task id
   * @return task info of the deleted task or <code>null</code> if no tasks have been deleted
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public TaskInfo updateTask(@RequestBody TaskDefinition taskDefinition, @PathVariable UUID taskId) {
    LOG.debug(String.format("POST /rest/harvester/tasks/%s <-- %s", taskId, taskDefinition));
    TaskDefinition oldTaskDef = engine.updateTaskDefinition(taskId, taskDefinition);
    return oldTaskDef!=null? new TaskInfo(taskId, oldTaskDef): null;
  }
}
