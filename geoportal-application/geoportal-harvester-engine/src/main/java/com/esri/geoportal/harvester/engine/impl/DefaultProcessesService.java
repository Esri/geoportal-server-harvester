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
package com.esri.geoportal.harvester.engine.impl;

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.ProcessesService;
import com.esri.geoportal.harvester.engine.managers.ProcessManager;
import com.esri.geoportal.harvester.engine.managers.ReportBuilder;
import com.esri.geoportal.harvester.engine.support.CrudsException;
import com.esri.geoportal.harvester.engine.support.ProcessReference;
import com.esri.geoportal.harvester.engine.support.ReportBuilderAdaptor;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default processes service.
 */
public class DefaultProcessesService implements ProcessesService {
  protected final ProcessManager processManager;
  protected final ReportBuilder reportBuilder;

  public DefaultProcessesService(ProcessManager processManager, ReportBuilder reportBuilder) {
    this.processManager = processManager;
    this.reportBuilder = reportBuilder;
  }

  @Override
  public ProcessInstance getProcess(UUID processId) throws DataProcessorException {
    try {
      return processManager.read(processId);
    } catch (CrudsException ex) {
      throw new DataProcessorException(String.format("Error getting process: %s", processId), ex);
    }
  }

  @Override
  public List<Map.Entry<UUID, ProcessInstance>> selectProcesses(Predicate<? super Map.Entry<UUID, ProcessInstance>> predicate) throws DataProcessorException {
    try {
      return processManager.select().stream().filter(predicate != null ? predicate : (Map.Entry<UUID, ProcessInstance> e) -> true).collect(Collectors.toList());
    } catch (CrudsException ex) {
      throw new DataProcessorException(String.format("Error celecting processes."), ex);
    }
  }

  @Override
  public ProcessReference createProcess(Task task) throws InvalidDefinitionException, DataProcessorException {
    try {
      ProcessInstance process = task.getProcessor().createProcess(task);
      UUID uuid = processManager.create(process);
      process.addListener(new ReportBuilderAdaptor(uuid, process, reportBuilder));
      return new ProcessReference(uuid, process);
    } catch (CrudsException ex) {
      throw new DataProcessorException(String.format("Error creating process: %s", task), ex);
    }
  }
}
