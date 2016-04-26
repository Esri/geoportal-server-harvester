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

import com.esri.geoportal.harvester.beans.EngineBean;
import com.esri.geoportal.harvester.engine.TaskDefinition;
import java.util.UUID;
import java.util.stream.Collectors;
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
  
  @Autowired
  private EngineBean engine;
  
  /**
   * Lists all available tasks.
   * @return array of task informations
   */
  @RequestMapping(value = "/rest/harvester/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public TaskInfo[] listTasks() {
    return engine.selectTaskDefinitions(null).stream().map(d->new TaskInfo(d.getKey(), d.getValue())).collect(Collectors.toList()).toArray(new TaskInfo[0]);
  }
  
  /**
   * Gets task by id.
   * @param taskId task id
   * @return task info or <code>null</code> if no task found
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public TaskInfo getTask(@PathVariable UUID taskId) {
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
    UUID id = engine.addTask(taskDefinition);
    return new TaskInfo(id, taskDefinition);
  }
  
}
