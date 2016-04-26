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
package com.esri.geoportal.harvester.engine;

import com.esri.geoportal.harvester.api.DataAdaptorDefinition;
import com.esri.geoportal.harvester.api.DataSource;
import com.esri.geoportal.harvester.api.DataSourceFactory;
import com.esri.geoportal.harvester.impl.DataDestinationRegistry;
import com.esri.geoportal.harvester.impl.DataSourceRegistry;
import com.esri.geoportal.harvester.api.DataDestinationFactory;
import com.esri.geoportal.harvester.api.DataDestination;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Harvesting engine.
 */
public class Engine {
  private final ReportBuilder reportBuilder;
  private final TaskManager taskManager;
  private final ProcessManager processManager;
  private final DataSourceRegistry dsReg;
  private final DataDestinationRegistry dpReg;

  /**
   * Creates instance of the engine.
   * @param reportBuilder report builder
   * @param taskManager task manager
   * @param processManager process manager
   * @param dsReg data source registry
   * @param dpReg data publisher registry
   */
  public Engine(ReportBuilder reportBuilder, TaskManager taskManager, ProcessManager processManager, DataSourceRegistry dsReg, DataDestinationRegistry dpReg) {
    this.reportBuilder = reportBuilder;
    this.taskManager = taskManager;
    this.processManager = processManager;
    this.dsReg = dsReg;
    this.dpReg = dpReg;
  }

  /**
   * Gets sources types.
   * @return collection of sources types
   */
  public Collection<String> getSourcesTypes() {
    return dsReg.keySet();
  }

  /**
   * Gets destinations types.
   * @return collection of destinations types
   */
  public Collection<String> getDestinationsTypes() {
    return dpReg.keySet();
  }

  /**
   * Gets process by process id.
   * @param processId process id.
   * @return process or <code>null</code> if no process available for the given process id
   */
  public Process getProcess(UUID processId) {
    return processManager.read(processId);
  }
  
  /**
   * Selects processes by predicate.
   * @param predicate predicate
   * @return list of processes matching predicate
   */
  public List<Map.Entry<UUID,Process>> selectProcesses(Predicate<? super Map.Entry<UUID, Process>> predicate) {
    return processManager.select().stream().filter(predicate!=null? predicate: (Map.Entry<UUID, Process> e) -> true).collect(Collectors.toList());
  }
  
  /**
   * Creates task to execute.
   * @param dsParams data source init parameters
   * @param dpParams data publisher init parameters
   * @return task
   */
  public Task createTask(DataAdaptorDefinition dsParams, List<DataAdaptorDefinition> dpParams) {
    DataSourceFactory dsFactory = dsReg.get(dsParams.getType());
    
    if (dsFactory==null) {
      throw new IllegalArgumentException("Invalid data source init parameters");
    }
    
    DataSource dataSource = dsFactory.create(dsParams.getAttributes());

    ArrayList<DataDestination> dataDestinations =  new ArrayList<>();
    for (DataAdaptorDefinition def: dpParams) {
      DataDestinationFactory dpFactory = dpReg.get(def.getType());
      if (dpFactory==null) {
        throw new IllegalArgumentException("Invalid data publisher init parameters");
      }

      DataDestination dataPublisher = dpFactory.create(def.getAttributes());
      dataDestinations.add(dataPublisher);
    }
    
    return new Task(dataSource, dataDestinations);
  }
  
  /**
   * Creates process.
   * @param task task for the process
   * @return process
   */
  public UUID createProcess(Task task) {
    Process process = new Process(reportBuilder, task);
    return processManager.create(process);
  }
  
  /**
   * Selects task definitions.
   * @param predicate predicate
   * @return list of task definitions matching predicate
   */
  public List<Map.Entry<UUID,TaskDefinition>> selectTaskDefinitions(Predicate<? super Map.Entry<UUID, TaskDefinition>> predicate) {
    return taskManager.select().stream().filter(predicate!=null? predicate: (Map.Entry<UUID, TaskDefinition> e) -> true).collect(Collectors.toList());
  }
}
