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

import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.utils.CrudlException;
import com.esri.geoportal.harvester.engine.utils.TriggerReference;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Triggers service.
 */
public interface TriggersService {

  /**
   * Lists all triggers.
   * @return list of triggers
   */
  List<Trigger> listTriggers();
  
  /**
   * Get trigger by type.
   * @param type trigger type
   * @return trigger or <code>null</code> if no trigger
   */
  Trigger getTrigger(String type);
  
  /**
   * Selects triggers.
   * @return collection of triggers.
   * @throws CrudlException if selecting fails
   */
  Collection<Map.Entry<UUID, TriggerManager.TaskUuidTriggerDefinitionPair>> select() throws CrudlException;
  
  /**
   * Deactivates trigger.
   * @param triggerInstanceUuid trigger uuid
   * @return trigger reference
   * @throws InvalidDefinitionException if invalid definition
   * @throws DataProcessorException if error processing data
   */
  TriggerReference deactivateTriggerInstance(UUID triggerInstanceUuid) throws InvalidDefinitionException, DataProcessorException;
  
  /**
   * Lists all activated triggers.
   * @return list of all activated triggers
   */
  List<TriggerReference> listActivatedTriggers();
  
  /**
   * Lists all activated triggers.
   * @param taskId task id
   * @return list of all activated triggers
   */
  List<TriggerReference> listActivatedTriggers(UUID taskId);
  
  /**
   * Activates trigger instances
   */
  void activateTriggerInstances();
  
  /**
   * Deactivates trigger instances.
   */
  void deactivateTriggerInstances();
}
