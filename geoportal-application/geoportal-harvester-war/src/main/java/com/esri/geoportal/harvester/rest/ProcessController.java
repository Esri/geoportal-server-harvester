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

import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.support.ProcessResponse;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.engine.managers.History;
import com.esri.geoportal.harvester.engine.managers.History.Event;
import com.esri.geoportal.harvester.engine.services.Engine;
import com.esri.geoportal.harvester.engine.utils.Statistics;
import com.esri.geoportal.harvester.support.ProcessStatisticsResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Process controller.
 * Provides access to processes.
 * <pre><code>
   GET /rest/harvester/processes                  - gets a list of all processes
   GET /rest/harvester/processes/{processId}      - gets a single process
   DELETE /rest/harvester/processes/{processId}   - aborts a single process
 * </code></pre>
 */
@RestController
public class ProcessController {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);
  
  @Autowired
  private Engine engine;
  
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
  public ResponseEntity<ProcessStatisticsResponse> getProcessInfo(@PathVariable UUID processId) {
    try {
      LOG.debug(formatForLog("GET /rest/harvester/processes/%s", processId));
      ProcessInstance process = engine.getProcessesService().getProcess(processId);
      Statistics statistics = engine.getProcessesService().getStatistics(processId);
      
      if (statistics==null) {
        // If statistics is null that mean the proces has completed (no longer in the memory).
        // Obtain statistics from the history.
        History history = engine.getTasksService().getHistory(UUID.fromString(process.getTask().getRef()));
        if (history!=null) {
          for (Event evt: history) {
            if (evt.getUuid().equals(processId)) {
              final History.Report rpt = evt.getReport();
              statistics = createStatistics(evt, rpt);
              break;
            }
          }
        }
      }
      
      return new ResponseEntity<>(process!=null? new ProcessStatisticsResponse(processId, process.getTask().getTaskDefinition(), process.getStatus(), statistics): null,HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error getting process info: %s", processId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  private Statistics createStatistics(Event evt, History.Report rpt) {
    return new Statistics() {
      @Override
      public Date getEndDate() {
        return evt.getEndTimestamp();
      }

      @Override
      public long getHarvestFailed() {
        return rpt.failedToHarvest;
      }

      @Override
      public long getPublishFailed() {
        return rpt.failedToPublish;
      }

      @Override
      public Date getStartDate() {
        return evt.getEndTimestamp();
      }

      @Override
      public long getAcquired() {
        return rpt.acquired;
      }

      @Override
      public long getSucceeded() {
        return rpt.created + rpt.updated;
      }

      @Override
      public boolean isFailure() {
        return rpt.failed > 0;
      }
    };
  }
  
  /**
   * Aborts (deletes) an existing process.
   * @param processId process id
   * @return process info
   */
  @RequestMapping(value = "/rest/harvester/processes/{processId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProcessResponse> abortProcess(@PathVariable UUID processId) {
    try {
      LOG.debug(formatForLog("DELETE /rest/harvester/processes/%s", processId));
      ProcessInstance process = engine.getProcessesService().getProcess(processId);
      if (process!=null) {
        try {
          process.abort();
        } catch (IllegalStateException ex) {
          LOG.warn("Unable to abort the process.", ex);
        }
      }
      return new ResponseEntity<>(process!=null? new ProcessResponse(
              processId, 
              process.getTask().getTaskDefinition(), 
              process.getStatus()): null, HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(formatForLog("Error aborting process: %s", processId), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Removes completed processes from the list of processes.
   * @return process info array
   */
  @RequestMapping(value = "/rest/harvester/processes", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProcessResponse[]> purge() {
    try {
      LOG.debug(String.format("DELETE /rest/harvester/processes"));
      List<Map.Entry<UUID, ProcessInstance>> completed = engine.getProcessesService().removeCompleted();
      return new ResponseEntity<>(completed.stream()
              .map(e->new ProcessResponse(
                      e.getKey(),
                      e.getValue().getTask().getTaskDefinition(), 
                      e.getValue().getStatus()))
              .collect(Collectors.toList()).toArray(new ProcessResponse[0]), HttpStatus.OK);
    } catch (DataProcessorException ex) {
      LOG.error(String.format("Error purging processes"), ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Filters processes.
   * @param predicate predicate
   * @return array of filtered processes
   */
  private ProcessResponse[] filterProcesses(Predicate<? super Map.Entry<UUID, ProcessInstance>> predicate) throws DataProcessorException {
    return engine.getProcessesService().selectProcesses(predicate).stream()
            .map(e->{ 
              Statistics statistics = null;
              try {
                History history = engine.getTasksService().getHistory(UUID.fromString(e.getValue().getTask().getRef()));
                if (history!=null) {
                  for (Event evt: history) {
                    if (evt.getUuid().equals( e.getKey())) {
                      final History.Report rpt = evt.getReport();
                      statistics = createStatistics(evt, rpt);
                      break;
                    }
                  }
                }
              } catch (DataProcessorException ex) {
              }
              return new ProcessStatisticsResponse(
                    e.getKey(),
                    e.getValue().getTask().getTaskDefinition(), 
                    e.getValue().getStatus(), statistics); 
            })
            .collect(Collectors.toList()).toArray(new ProcessResponse[0]);
  }
  
}
