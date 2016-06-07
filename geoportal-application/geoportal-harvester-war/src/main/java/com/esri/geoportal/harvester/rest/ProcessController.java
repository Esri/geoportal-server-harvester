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

import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.support.ProcessResponse;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.beans.EngineBean;
import com.esri.geoportal.harvester.engine.support.ProcessReference;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static com.esri.geoportal.harvester.engine.support.JsonSerializer.deserialize;

/**
 * Process controller.
 * Provides access to processes.
 * <pre><code>
   GET /rest/harvester/processes
   GET /rest/harvester/processes/{processId}
   DELETE /rest/harvester/processes/{processId}
   PUT /rest/harvester/processes
 * </code></pre>
 */
@RestController
public class ProcessController {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);
  
  @Autowired
  private EngineBean engine;
  
  /**
   * List all processes.
   * @return all processes
   */
  @RequestMapping(value = "/rest/harvester/processes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProcessResponse[]> listAllProcesses() {
    try {
      LOG.debug(String.format("GET /rest/harvester/processes"));
      return new ResponseEntity<>(filterProcesses(e->true),HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error listing all processes"), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Get access to the given process.
   * @param processId process id
   * @return process info
   */
  @RequestMapping(value = "/rest/harvester/processes/{processId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProcessResponse> getProcessInfo(@PathVariable UUID processId) {
    try {
      LOG.debug(String.format("GET /rest/harvester/processes/%s", processId));
      Processor.Process process = engine.getProcess(processId);
      return new ResponseEntity<>(process!=null? new ProcessResponse(processId, process.getTitle(), process.getStatus()): null,HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error getting process info: %s", processId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Aborts (deletes) an existing process.
   * @param processId process id
   * @return process info
   */
  @RequestMapping(value = "/rest/harvester/processes/{processId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProcessResponse> abortProcess(@PathVariable UUID processId) {
    try {
      LOG.debug(String.format("DELETE /rest/harvester/processes/%s", processId));
      Processor.Process process = engine.getProcess(processId);
      if (process!=null) {
        try {
          process.abort();
        } catch (IllegalStateException ex) {
          LOG.warn("Unable to abort the process.", ex);
        }
      }
      return new ResponseEntity<>(process!=null? new ProcessResponse(processId, process.getTitle(), process.getStatus()): null, HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error aborting process: %s", processId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Allows to define new process.
   * <p>
   * Newly defined process is started immediately. Example of the POST data of
   * a process designed to harvest from WAF to a local folder:
   * <pre><code>
{
    "processor": null,
    "source": {
        "type": "WAF",
        "properties": {
            "waf-host-url": "http://gptsrv12r2/wafMetadata/metadataSamples/"
        }
    },
    "destinations": [
        {
            "type": "FOLDER",
            "properties": {
                "folder-root-folder": "c:\\data"
            }
        }
    ]
}
   * </code></pre>
   * @param taskDef task definition
   * @return process info
   */
  @RequestMapping(value = "/rest/harvester/processes", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProcessResponse> createProcess(@RequestBody String taskDef) {
    try {
      LOG.debug(String.format("PUT /rest/harvester/processes <-- %s", taskDef));
      TaskDefinition taskDefinition = deserialize(engine,taskDef);
      ProcessReference ref = engine.submitTaskDefinition(taskDefinition);
      ref.getProcess().begin();
      return new ResponseEntity<>(new ProcessResponse(ref.getProcessId(), ref.getProcess().getTitle(), ref.getProcess().getStatus()), HttpStatus.OK);
    } catch (InvalidDefinitionException ex) {
      LOG.error(String.format("Error creating process: %s", taskDef), ex);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (IOException|DataProcessorException ex) {
      LOG.error(String.format("Error creating process: %s", taskDef), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Filters processes.
   * @param predicate predicate
   * @return array of filtered processes
   */
  private ProcessResponse[] filterProcesses(Predicate<? super Map.Entry<UUID, Processor.Process>> predicate) throws DataProcessorException {
    return engine.selectProcesses(predicate).stream()
            .map(e->new ProcessResponse(e.getKey(),e.getValue().getTitle(),e.getValue().getStatus()))
            .collect(Collectors.toList()).toArray(new ProcessResponse[0]);
  }
  
}
