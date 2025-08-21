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
package com.esri.geoportal.harvester.engine.services;

import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker.IteratorContext;
import com.esri.geoportal.harvester.engine.utils.ProcessReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Execution service.
 */
public interface ExecutionService {

  /**
   * Executes task.
   *
   * @param taskDefinition task definition
   * @param iteratorContext iterator context
   * @return process handle
   * @throws InvalidDefinitionException invalid definition exception
   * @throws DataProcessorException if accessing repository fails
   */
  ProcessReference execute(TaskDefinition taskDefinition, IteratorContext iteratorContext) throws InvalidDefinitionException, DataProcessorException,TimeoutException,ExecutionException,InterruptedException;
}
