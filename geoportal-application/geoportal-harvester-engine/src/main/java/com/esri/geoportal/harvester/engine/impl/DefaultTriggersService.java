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
import com.esri.geoportal.harvester.engine.ExecutionService;
import com.esri.geoportal.harvester.engine.TriggersService;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager.TaskUuidTriggerInstancePair;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.registers.TriggerRegistry;
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
  protected final ExecutionService executionService;

  /**
   * Creates instance of the service.
   * @param triggerRegistry trigger registry
   * @param triggerManager trigger manager
   * @param triggerInstanceManager trigger instance manager
   * @param executionService execution service
   */
  public DefaultTriggersService(TriggerRegistry triggerRegistry, TriggerManager triggerManager, TriggerInstanceManager triggerInstanceManager, ExecutionService executionService) {
    this.triggerRegistry = triggerRegistry;
    this.triggerManager = triggerManager;
    this.triggerInstanceManager = triggerInstanceManager;
    this.executionService = executionService;
  }

  @Override
  public List<Trigger> listTriggers() {
    return new ArrayList<>(triggerRegistry.values());
  }
  
  @Override
  public Trigger getTrigger(String type) {
    return triggerRegistry.get(type);
  }
  
  @Override
  public Collection<Map.Entry<UUID, TriggerManager.TaskUuidTriggerDefinitionPair>> select() throws CrudsException {
    return triggerManager.select();
  }
  
  @Override
  public TriggerReference deactivateTriggerInstance(UUID triggerInstanceUuid) throws InvalidDefinitionException, DataProcessorException {
    TaskUuidTriggerInstancePair pair = triggerInstanceManager.remove(triggerInstanceUuid);
    if (pair == null) {
      throw new InvalidDefinitionException(String.format("Invalid trigger id: %s", triggerInstanceUuid));
    }
    try {
      TriggerDefinition trigDef = pair.getTriggerInstance().getTriggerDefinition();
      TriggerReference triggerReference = new TriggerReference(triggerInstanceUuid, pair.getTaskId(), trigDef);
      pair.getTriggerInstance().close();
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
            .map(e->new TriggerReference(e.getKey(), e.getValue().getTaskId(), e.getValue().getTriggerInstance().getTriggerDefinition()))
            .collect(Collectors.toList());
  }
  
  
  @Override
  public void activateTriggerInstances() {
    try {
      select().forEach(e->{
        UUID uuid = e.getKey();
        TriggerManager.TaskUuidTriggerDefinitionPair definition = e.getValue();
        
        try {
          Trigger trigger = getTrigger(definition.getTriggerDefinition().getType());
          if (trigger==null) {
            throw new InvalidDefinitionException(String.format("Invalid trigger type: %s", definition.getTriggerDefinition().getType()));
          }
          TriggerInstance triggerInstance = trigger.createInstance(definition.getTriggerDefinition());
          TriggerInstance.Context context = executionService.newTriggerContext(definition.getTaskUuid());
          triggerInstance.activate(context);
        } catch (DataProcessorException|InvalidDefinitionException ex) {
          LOG.warn(String.format("Error creating and activating trigger instance: %s -> %s", uuid, definition), ex);
        }
      });
    } catch (CrudsException ex) {
      LOG.error("Error processing trigger definitions", ex);
    }
  }
  
  @Override
  public void deactivateTriggerInstances() {
    triggerInstanceManager.listAll().stream().forEach(e->{
      UUID uuid = e.getKey();
      TriggerInstance triggerInstance = e.getValue().getTriggerInstance();
      
      try {
        triggerInstance.close();
      } catch (Exception ex) {
        LOG.warn(String.format("Error deactivating trigger instance: %s --> %s", uuid, triggerInstance.getTriggerDefinition()), ex);
      }
    });
    triggerInstanceManager.clear();
  }
}
