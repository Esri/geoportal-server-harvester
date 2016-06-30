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

import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.TriggerInstance;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.TriggersService;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.managers.TriggerRegistry;
import com.esri.geoportal.harvester.engine.support.CrudsException;
import com.esri.geoportal.harvester.engine.support.TriggerReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default triggers service.
 */
public class DefaultTriggersService implements TriggersService {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultTriggersService.class);
  
  protected final TriggerRegistry triggerRegistry;
  protected final TriggerManager triggerManager;
  protected final TriggerInstanceManager triggerInstanceManager;

  public DefaultTriggersService(TriggerRegistry triggerRegistry, TriggerManager triggerManager, TriggerInstanceManager triggerInstanceManager) {
    this.triggerRegistry = triggerRegistry;
    this.triggerManager = triggerManager;
    this.triggerInstanceManager = triggerInstanceManager;
  }

  @Override
  public List<Trigger> listTriggers() {
    return new ArrayList<>(triggerRegistry.values());
  }
  
  @Override
  public Trigger getTrigger(String type) {
    return triggerRegistry.get(type);
  }
  
  /**
   * Select triggers.
   * @return
   * @throws CrudsException 
   */
  @Override
  public Collection<Map.Entry<UUID, TriggerManager.TriggerDefinitionUuidPair>> select() throws CrudsException {
    return triggerManager.select();
  }
  
  @Override
  public void clear() {
    triggerInstanceManager.clear();
  }
  
  @Override
  public List<Map.Entry<UUID, TriggerInstance>> listAll() {
    return triggerInstanceManager.listAll();
  }
  
  @Override
  public TriggerReference deactivateTriggerInstance(UUID triggerInstanceUuid) throws InvalidDefinitionException, DataProcessorException {
    TriggerInstance triggerInstance = triggerInstanceManager.remove(triggerInstanceUuid);
    if (triggerInstance == null) {
      throw new InvalidDefinitionException(String.format("Invalid trigger id: %s", triggerInstanceUuid));
    }
    try {
      TriggerDefinition trigDef = triggerInstance.getTriggerDefinition();
      TriggerReference triggerReference = new TriggerReference(triggerInstanceUuid, trigDef);
      triggerInstance.close();
      return triggerReference;
    } catch (Exception ex) {
      throw new DataProcessorException(String.format("Error deactivating trigger: %s", triggerInstanceUuid), ex);
    } finally {
      try {
        triggerManager.delete(triggerInstanceUuid);
      } catch (CrudsException ex) {
        LOG.warn(String.format("Error deleting trigger: %s", triggerInstanceUuid), ex);
      }
    }
  }

  @Override
  public List<TriggerReference> listActivatedTriggers() {
    return triggerInstanceManager.listAll().stream()
            .map(e->new TriggerReference(e.getKey(),e.getValue().getTriggerDefinition()))
            .collect(Collectors.toList());
  }
}
