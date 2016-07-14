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
package com.esri.geoportal.harvester.engine;

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.support.ProcessReference;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Processes service.
 */
public interface ProcessesService {

  /**
   * Creates process.
   *
   * @param task task for the process
   * @param attributes attributes or <code>null</code> if no attributes
   * @return process handle
   * @throws InvalidDefinitionException if processor definition is invalid
   * @throws DataProcessorException if accessing repository fails
   */
  ProcessReference createProcess(Task task, Map<String,Object> attributes) throws InvalidDefinitionException, DataProcessorException;
  
  /**
   * Gets process by process id.
   *
   * @param processId process id.
   * @return process or <code>null</code> if no process available for the given
   * process id
   * @throws DataProcessorException if accessing repository fails
   */
  ProcessInstance getProcess(UUID processId) throws DataProcessorException;

  /**
   * Selects processes by predicate.
   *
   * @param predicate predicate
   * @return list of processes matching predicate
   * @throws DataProcessorException if accessing repository fails
   */
  List<Map.Entry<UUID, ProcessInstance>> selectProcesses(Predicate<? super Map.Entry<UUID, ProcessInstance>> predicate) throws DataProcessorException;
}
