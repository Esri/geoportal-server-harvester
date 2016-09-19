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
package com.esri.geoportal.harvester.engine.defaults;

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker.IteratorContext;
import com.esri.geoportal.harvester.engine.services.ProcessesService;
import com.esri.geoportal.harvester.engine.managers.ProcessManager;
import com.esri.geoportal.harvester.engine.managers.ReportManager;
import com.esri.geoportal.harvester.engine.registers.StatisticsRegistry;
import com.esri.geoportal.harvester.engine.utils.CrudlException;
import com.esri.geoportal.harvester.engine.utils.ProcessReference;
import com.esri.geoportal.harvester.engine.utils.ReportBuilder;
import com.esri.geoportal.harvester.engine.utils.ReportBuilderAdaptor;
import com.esri.geoportal.harvester.engine.utils.Statistics;
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
  protected final ReportManager reportManager;
  protected final StatisticsRegistry statisticsRegistry;

  /**
   * Creates instance of the service.
   * @param processManager process manager
   * @param reportManager report manager
   * @param statisticsRegistry statistics registry
   */
  public DefaultProcessesService(ProcessManager processManager, ReportManager reportManager, StatisticsRegistry statisticsRegistry) {
    this.processManager = processManager;
    this.reportManager = reportManager;
    this.statisticsRegistry = statisticsRegistry;
  }

  @Override
  public ProcessInstance getProcess(UUID processId) throws DataProcessorException {
    try {
      return processManager.read(processId);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error getting process: %s", processId), ex);
    }
  }

  @Override
  public Statistics getStatistics(UUID processId) throws DataProcessorException {
    return statisticsRegistry.get(processId);
  }

  @Override
  public List<Map.Entry<UUID, ProcessInstance>> selectProcesses(Predicate<? super Map.Entry<UUID, ProcessInstance>> predicate) throws DataProcessorException {
    try {
      return processManager.list().stream().filter(predicate != null ? predicate : (Map.Entry<UUID, ProcessInstance> e) -> true).collect(Collectors.toList());
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error celecting processes."), ex);
    }
  }

  @Override
  public ProcessReference createProcess(Task task, IteratorContext iteratorContext) throws InvalidDefinitionException, DataProcessorException {
    try {
      ProcessInstance process = task.getProcessor().createProcess(task,iteratorContext);
      UUID uuid = processManager.create(process);
      ReportBuilder reportBuilder = reportManager.createReportBuilder(uuid, process);
      process.addListener(new ReportBuilderAdaptor(uuid, process, reportBuilder));
      return new ProcessReference(uuid, process);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error creating process: %s", task), ex);
    }
  }
}
