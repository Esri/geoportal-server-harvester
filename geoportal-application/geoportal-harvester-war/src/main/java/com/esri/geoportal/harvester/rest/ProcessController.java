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

import com.esri.geoportal.harvester.support.ProcessInfo;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.TaskDefinition;
import com.esri.geoportal.harvester.beans.EngineBean;
import com.esri.geoportal.harvester.engine.Process;
import com.esri.geoportal.harvester.engine.Task;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Process controller.
 */
@RestController
public class ProcessController {
  
  @Autowired
  private EngineBean engine;
  
  /**
   * List all processes.
   * @return all processes
   */
  @RequestMapping(value = "/rest/harvester/processes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ProcessInfo[] listAllProcesses() {
    return filterProcesses(e->true);
  }
  
  /**
   * Get access to the given process.
   * @param processId process id
   * @return process info
   */
  @RequestMapping(value = "/rest/harvester/processes/{processId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ProcessInfo getProcessInfo(@PathVariable UUID processId) {
    Process process = engine.getProcess(processId);
    return process!=null? new ProcessInfo(processId, process.getDescription(), process.getStatus()): null;
  }
  
  /**
   * Aborts (deletes) an existing process.
   * @param processId process id
   * @return process info
   */
  @RequestMapping(value = "/rest/harvester/processes/{processId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ProcessInfo abortProcess(@PathVariable UUID processId) {
    Process process = engine.getProcess(processId);
    if (process!=null) {
      try {
        process.abort();
      } catch (IllegalStateException ex) {
        // ignore
      }
    }
    return process!=null? new ProcessInfo(processId, process.getDescription(), process.getStatus()): null;
  }
  
  /**
   * Allows to define new process.
   * <p>
   * Newly defined process is started immediately. Example of the POST data of
   * a process designed to harvest from WAF to a local folder:
   * <pre><code>
{
    "source": {
        "type": "WAF",
        "attributes": {
            "waf.host.url": "http://gptsrv12r2/wafMetadata/metadataSamples"
        }
    },
    "destinations": [
        {
            "type": "FOLDER",
            "attributes": {
                "folder.host.url": "http://gptsrv12r2",
                "folder.root.folder": "c:\\data"
            }
        }
    ]
}   
   * </code></pre>
   * @param taskDefinition task definition
   * @return process info
   */
  @RequestMapping(value = "/rest/harvester/processes", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProcessInfo> createProcess(@RequestBody TaskDefinition taskDefinition) {
    try {
      Task task = engine.createTask(taskDefinition.getSource(), taskDefinition.getDestinations());
      UUID processId = engine.createProcess(task);
      Process process = engine.getProcess(processId);
      process.begin();
      return new ResponseEntity<ProcessInfo>(new ProcessInfo(processId, process.getDescription(), process.getStatus()), HttpStatus.OK);
    } catch (InvalidDefinitionException ex) {
      return new ResponseEntity<ProcessInfo>(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Filters processes.
   * @param predicate predicate
   * @return array of filtered processes
   */
  private ProcessInfo[] filterProcesses(Predicate<? super Map.Entry<UUID, Process>> predicate) {
    return engine.selectProcesses(predicate).stream()
            .map(e->new ProcessInfo(e.getKey(),e.getValue().getDescription(),e.getValue().getStatus()))
            .collect(Collectors.toList()).toArray(new ProcessInfo[0]);
  }
  
}
