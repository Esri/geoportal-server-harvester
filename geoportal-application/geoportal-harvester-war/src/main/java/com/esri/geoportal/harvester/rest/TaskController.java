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

import com.esri.geoportal.commons.constants.MimeType;
import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.commons.utils.TextScrambler;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.base.SimpleIteratorContext;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.support.TaskResponse;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.managers.History;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.services.Engine;
import com.esri.geoportal.harvester.engine.utils.HistoryManagerAdaptor;
import static com.esri.geoportal.harvester.engine.utils.JsonSerializer.deserialize;
import com.esri.geoportal.harvester.engine.utils.ProcessReference;
import com.esri.geoportal.harvester.engine.utils.TriggerReference;
import com.esri.geoportal.harvester.support.EventResponse;
import com.esri.geoportal.harvester.support.ProcessResponse;
import com.esri.geoportal.harvester.support.TriggerResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Task controller.
 * <p>
 * Provides access to tasks.
 * <pre><code>
 * GET /rest/harvester/tasks                      - gets all the tasks
 * GET /rest/harvester/tasks/{taskId}             - gets task information by task id
 * DELETE /rest/harvester/tasks/{taskId}          - deletes existing task by task id
 * POST /rest/harvester/tasks                     - creates a new task (task definition in the request body)
 * PUT /rest/harvester/tasks/{taskId}             - updates a task by task id (task definition in the request body)
 * GET /rest/harvester/tasks/{taskId}/history     - gets task harvesting history
 *
 * POST /rest/harvester/tasks/{taskId}/execute    - executes immediatelly a task by task id
 * POST /rest/harvester/tasks/{taskId}/schedule   - schedule a task by task id (trigger definition in the request body)
 * POST /rest/harvester/tasks/execute             - executes a task (task definition in the request body)
 * POST /rest/harvester/tasks/schedule            - schedules a task (trigger instance definition in the request body)
 * </code></pre> Top five end points provide access to the stored task
 * definitions (CRUD). Remaining end points ('execute' and 'schedule') provide
 * ability to create process instance based on either stored task definition or
 * provided task definition.
 * <br>
 * Example of 'execute' request body:
 * <pre><code>
 *
 * PUT /rest/harvester/tasks/execute
 * {
 *  "processor": null,
 *  "source": {
 *    "type": "WAF",
 *    "properties": {
 *      "waf-host-url": "http://gptsrv12r2/wafMetadata/metadataSamples/"
 *    }
 *  },
 *  "destinations": [
 *    {
 *      "type": "FOLDER",
 *      "properties": {
 *        "folder-root-folder": "c:\\data"
 *      }
 *    }
 *  ]
 * }
 * </code></pre>
 * <br>
 * Example of 'execute' particular task request (no body expected):
 * <pre><code>
 *
 * POST /rest/harvester/tasks/641c741a-37f9-11e6-ac61-9e71128cae77/execute
 * </code></pre>
 * <br>
 * Example of 'schedule' request body:
 * <pre><code>
 *
 * POST /rest/harvester/tasks/schedule
 * {
 *  "type": "AT",
 *  "properties": {
 *    "t-at-time": "10:00"
 *  },
 *  "taskDefinition": {
 *    "processor": null,
 *    "source": {
 *      "type": "WAF",
 *      "properties": {
 *        "waf-host-url": "http://gptsrv12r2/wafMetadata/metadataSamples/"
 *      }
 *    },
 *    "destinations": [
 *      {
 *        "type": "FOLDER",
 *        "properties": {
 *          "folder-root-folder": "c:\\data"
 *        }
 *      }
 *    ]
 *  }
 * }
 * </code></pre>
 * <br>
 * Example of 'schedule' particular task request body:
 * <pre><code>
 *
 * POST /rest/harvester/tasks/31b4f880-37f9-11e6-ac61-9e71128cae77/schedule
 * {
 *  "type": "AT",
 *  "properties": {
 *    "t-at-time": "10:00"
 *  }
 * }
 * </code></pre>
 */
@RestController
public class TaskController {

  private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);

  @Autowired
  private Engine engine;

  @Autowired
  private HistoryManager historyManager;

  /**
   * Lists all available tasks.
   *
   * @return array of task informations
   */
  @RequestMapping(value = "/rest/harvester/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TaskResponse[]> listTasks() {
    try {
      LOG.debug(String.format("GET /rest/harvester/tasks"));
      return new ResponseEntity<>(engine.getTasksService().selectTaskDefinitions(null).stream().map(d -> new TaskResponse(d.getKey(), d.getValue())).collect(Collectors.toList()).toArray(new TaskResponse[0]), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error listing tasks."), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Gets task by id.
   *
   * @param taskId task id
   * @return task info or <code>null</code> if no task found
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TaskResponse> getTask(@PathVariable UUID taskId) {
    try {
      LOG.debug(formatForLog("GET /rest/harvester/tasks/%s", taskId));
      TaskDefinition taskDefinition = engine.getTasksService().readTaskDefinition(taskId);
      if (taskDefinition == null) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
      return new ResponseEntity<>(new TaskResponse(taskId, taskDefinition), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error getting task: %s", taskId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Gets task by id.
   *
   * @param taskId task id
   * @return task info or <code>null</code> if no task found
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}/export", method = RequestMethod.GET)
  public ResponseEntity<String> export(@PathVariable UUID taskId) {
    try {
      LOG.debug(formatForLog("GET /rest/harvester/tasks/%s", taskId));
      TaskDefinition taskDefinition = engine.getTasksService().readTaskDefinition(taskId);
      if (taskDefinition == null) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
      String fileName = StringUtils.trimToEmpty(taskDefinition.getName());
      if (fileName.isEmpty()) {
        fileName = taskId.toString();
      } else {
        fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
      }
      headers.add("Content-disposition", String.format("attachment; filename=\"%s.json\"", fileName));
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      ResponseEntity<String> responseEntity = new ResponseEntity<>(mapper.writeValueAsString(taskDefinition), headers, HttpStatus.OK);
      return responseEntity;
    } catch (DataProcessorException | JsonProcessingException ex) {
      LOG.error(formatForLog("Error getting task: %s", taskId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Gets history by task id.
   *
   * @param taskId task id
   * @return list of all events for the given task
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}/history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<EventResponse>> getTaskHistory(@PathVariable UUID taskId) {
    try {
      LOG.debug(formatForLog("GET /rest/harvester/tasks/%s/history", taskId));
      List<History.Event> history = engine.getTasksService().getHistory(taskId);
      return new ResponseEntity<>(history.stream().map(e -> new EventResponse(e)).collect(Collectors.toList()), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error getting task: %s", taskId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Gets history by task id.
   *
   * @param taskId task id
   * @return list of all events for the given task
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}/history", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> purgeHistory(@PathVariable UUID taskId) {
    try {
      LOG.debug(formatForLog("DELETE /rest/harvester/tasks/%s/history", taskId));
      engine.getTasksService().purgeHistory(taskId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error purging history for task: %s", taskId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Deletes task by id.
   *
   * @param taskId task id
   * @return task info of the deleted task or <code>null</code> if no tasks have
   * been deleted
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TaskResponse> deleteTask(@PathVariable UUID taskId) {
    try {
      LOG.debug(formatForLog("DELETE /rest/harvester/tasks/%s", taskId));
      TaskDefinition taskDefinition = engine.getTasksService().readTaskDefinition(taskId);
      if (taskDefinition != null) {
        List<TriggerReference> triggers = engine.getTriggersService().listActivatedTriggers(taskId);
        for (TriggerReference ref: triggers) {
          engine.getTriggersService().deactivateTriggerInstance(ref.getUuid());
        }
        engine.getTasksService().deleteTaskDefinition(taskId);
      }
      return new ResponseEntity<>(new TaskResponse(taskId, taskDefinition), HttpStatus.OK);
    } catch (DataProcessorException|InvalidDefinitionException ex) {
      LOG.error(formatForLog("Error deleting task: %s", taskId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Adds a new task.
   *
   * @param taskDefinition task definition
   * @return task info of the newly created task
   */
  @RequestMapping(value = "/rest/harvester/tasks", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TaskResponse> addTask(@RequestBody TaskDefinition taskDefinition) {
    try {
      LOG.debug(formatForLog("POST /rest/harvester/tasks <-- %s", taskDefinition));
      UUID id = engine.getTasksService().addTaskDefinition(taskDefinition);
      return new ResponseEntity<>(new TaskResponse(id, taskDefinition), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error adding task: %s", taskDefinition), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Updates task by id.
   *
   * @param taskDefinition task definition
   * @param taskId task id
   * @return task info of the deleted task or <code>null</code> if no tasks have
   * been deleted
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TaskResponse> updateTask(@RequestBody TaskDefinition taskDefinition, @PathVariable UUID taskId) {
    try {
      LOG.debug(formatForLog("PUT /rest/harvester/tasks/%s <-- %s", taskId, taskDefinition));
      TaskDefinition oldTaskDef = engine.getTasksService().updateTaskDefinition(taskId, taskDefinition);
      return new ResponseEntity<>(oldTaskDef != null ? new TaskResponse(taskId, oldTaskDef) : null, HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error updating task: %s <-- %s", taskId, taskDefinition), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Executes task by id.
   *
   * @param taskId task id
   * @param ignoreRobots optional flag to ignore robots.txt
   * @param incremental optional incremental harvest flag
   * @return task info of the deleted task or <code>null</code> if no tasks have
   * been deleted
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}/execute", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProcessResponse> executeTask(@PathVariable UUID taskId, @RequestParam(required = false) Boolean ignoreRobots, @RequestParam(required = false) Boolean incremental) {
    try {
      LOG.debug(formatForLog("POST /rest/harvester/tasks/%s/execute", taskId));
      TaskDefinition taskDefinition = engine.getTasksService().readTaskDefinition(taskId);
      if (taskDefinition == null) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
      if (incremental == null) {
        incremental = taskDefinition.isIncremental();
      } else {
        taskDefinition.setIncremental(incremental);
      }
      if (ignoreRobots!=null) {
        taskDefinition.setIgnoreRobotsTxt(ignoreRobots);
      }

      // obtain last harvest data
      History history = engine.getTasksService().getHistory(taskId);
      History.Event lastEvent = history != null ? history.getLastEvent() : null;

      // make iterator context
      SimpleIteratorContext iteratorContext = new SimpleIteratorContext();
      iteratorContext.setLastHarvest(incremental && lastEvent != null ? lastEvent.getStartTimestamp() : null);

      ProcessReference ref = engine.getExecutionService().execute(taskDefinition, iteratorContext);
      ref.getProcess().addListener(new HistoryManagerAdaptor(taskId, ref.getProcess(), historyManager));
      ref.getProcess().init();
      ref.getProcess().begin();
      return new ResponseEntity<>(new ProcessResponse(
              ref.getProcessId(), 
              taskDefinition, 
              ref.getProcess().getStatus()), HttpStatus.OK);
    } catch (InvalidDefinitionException ex) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (DataProcessorException ex) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Executes task immediately using task definition.
   *
   * @param taskDefinition task definition
   * @return task info of the deleted task or <code>null</code> if no tasks have
   * been deleted
   */
  @RequestMapping(value = "/rest/harvester/tasks/execute", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProcessResponse> executeTask(@RequestBody TaskDefinition taskDefinition) {
    try {
      LOG.debug(formatForLog("POST /rest/harvester/tasks/execute <-- %s", taskDefinition));
      ProcessReference ref = engine.getExecutionService().execute(taskDefinition, new SimpleIteratorContext());
      ref.getProcess().init();
      ref.getProcess().begin();
      return new ResponseEntity<>(new ProcessResponse(
              ref.getProcessId(), 
              taskDefinition, 
              ref.getProcess().getStatus()), HttpStatus.OK);
    } catch (InvalidDefinitionException ex) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (DataProcessorException ex) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Schedules task by id.
   *
   * @param triggerDefinition trigger definition
   * @param taskId task id
   * @param ignoreRobots optional flag to ignore robots.txt
   * @param incremental optional incremental harvest flag
   * @return task info of the deleted task or <code>null</code> if no tasks have
   * been deleted
   */
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}/schedule", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TriggerResponse> scheduleTask(@RequestBody EntityDefinition triggerDefinition, @PathVariable UUID taskId, @RequestParam(required = false) Boolean ignoreRobots, @RequestParam(required = false) Boolean incremental) {
    try {
      LOG.debug(formatForLog("POST /rest/harvester/tasks/%s/schedule <-- %s", taskId, triggerDefinition));
      TaskDefinition taskDefinition = engine.getTasksService().readTaskDefinition(taskId);
      if (taskDefinition == null) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
      if (incremental == null) {
        incremental = taskDefinition.isIncremental();
      } else {
        taskDefinition.setIncremental(incremental);
      }
      if (ignoreRobots!=null) {
        taskDefinition.setIgnoreRobotsTxt(ignoreRobots);
      }

      TriggerDefinition triggerInstanceDefinition = new TriggerDefinition();
      triggerInstanceDefinition.setType(triggerDefinition.getType());
      triggerInstanceDefinition.setTaskDefinition(taskDefinition);
      triggerInstanceDefinition.setProperties(triggerDefinition.getProperties());

      // obtain last harvest data
      History history = engine.getTasksService().getHistory(taskId);
      History.Event lastEvent = history != null ? history.getLastEvent() : null;

      // make iterator context
      SimpleIteratorContext iteratorContext = new SimpleIteratorContext();
      iteratorContext.setLastHarvest(incremental && lastEvent != null ? lastEvent.getStartTimestamp() : null);

      TriggerReference trigRef = engine.getTriggersService().schedule(taskId, triggerInstanceDefinition, iteratorContext);
      return new ResponseEntity<>(new TriggerResponse(trigRef.getUuid(), taskId, trigRef.getTriggerDefinition()), HttpStatus.OK);
    } catch (InvalidDefinitionException ex) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (DataProcessorException ex) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/rest/harvester/tasks/{taskId}/triggers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<TriggerResponse>> listTriggers(@PathVariable UUID taskId) {
    LOG.debug(formatForLog("GET /rest/harvester/tasks/%s/triggers", taskId));
    List<TriggerResponse> triggerResponses = engine.getTriggersService().listActivatedTriggers(taskId).stream()
            .map(t -> new TriggerResponse(t.getUuid(), t.getTaskId(), t.getTriggerDefinition()))
            .collect(Collectors.toList());
    return new ResponseEntity<>(triggerResponses, HttpStatus.OK);
  }

  /**
   * Schedules task using trigger definition.
   *
   * @param trigDef trigger definition
   * @return task info of the deleted task or <code>null</code> if no tasks have
   * been deleted
   */
  @RequestMapping(value = "/rest/harvester/tasks/schedule", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TriggerResponse> scheduleTask(@RequestBody TriggerDefinition trigDef) {
    try {
      LOG.debug(formatForLog("POST /rest/harvester/tasks/schedule <-- %s", trigDef));
      TriggerReference trigRef = engine.getTriggersService().schedule(null, trigDef, new SimpleIteratorContext());
      return new ResponseEntity<>(new TriggerResponse(trigRef.getUuid(), null, trigRef.getTriggerDefinition()), HttpStatus.OK);
    } catch (InvalidDefinitionException ex) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (DataProcessorException ex) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/rest/harvester/tasks/upload", method = RequestMethod.POST)
  public ResponseEntity<TaskResponse> handleImport(@RequestParam("file") MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      try {
        TaskDefinition taskDefinition = deserialize(inputStream,TaskDefinition.class);
        LOG.debug(formatForLog("POST /rest/harvester/tasks/upload <-- %s", taskDefinition));
        UUID id = engine.getTasksService().addTaskDefinition(taskDefinition);
        return new ResponseEntity<>(new TaskResponse(id, taskDefinition), HttpStatus.OK);
      } catch (IOException|DataProcessorException ex) {
        LOG.error(String.format("Error uploading task"), ex);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } catch (IOException ex) {
      LOG.error(String.format("Error uploading task"), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  @RequestMapping(value = "/rest/harvester/tasks/{taskId}/record", method = RequestMethod.POST) 
  public ResponseEntity<byte[]> handleRecord(@PathVariable UUID taskId, @RequestParam(required = true) String id, @RequestParam(required = false) String userName, @RequestParam(required = false) String password) {
    try {
      TaskDefinition taskDefinition = engine.getTasksService().readTaskDefinition(taskId);
      if (taskDefinition == null) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
      
      SimpleCredentials credentials = !StringUtils.isBlank(userName) && !StringUtils.isBlank(password)
              ? new SimpleCredentials(userName, TextScrambler.decode(password))
              : null;
      
      DataContent content = engine.getTasksService().fetchContent(taskId, id, credentials);
      
      MimeType mimeType = content.getContentType().stream().findFirst().orElse(null);
      if (mimeType != null) {
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", mimeType.getName());
        return new ResponseEntity<>(content.getContent(mimeType), headers, HttpStatus.OK);
      } else {
        return new ResponseEntity<>(new byte[0], HttpStatus.OK);
      }
    } catch (DataException ex) {
      LOG.error(formatForLog("Error fetching record: %s <-- %s", taskId, taskId), ex);
      if (ex.getCause() instanceof HttpResponseException) {
        HttpResponseException cause = (HttpResponseException)ex.getCause();
        return new ResponseEntity<>(HttpStatus.resolve(cause.getStatusCode()));
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (IOException ex) {
      LOG.error(formatForLog("Error fetching record: %s <-- %s", taskId, taskId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
