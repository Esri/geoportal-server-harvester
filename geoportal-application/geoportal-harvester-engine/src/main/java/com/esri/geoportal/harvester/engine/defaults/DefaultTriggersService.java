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

import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.TriggerInstance;
import com.esri.geoportal.harvester.api.base.SimpleIteratorContext;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.engine.managers.History;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.services.ExecutionService;
import com.esri.geoportal.harvester.engine.services.TriggersService;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager.TaskUuidTriggerInstancePair;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.registers.TriggerRegistry;
import com.esri.geoportal.harvester.engine.utils.CrudlException;
import com.esri.geoportal.harvester.engine.utils.HistoryManagerAdaptor;
import com.esri.geoportal.harvester.engine.utils.ProcessReference;
import com.esri.geoportal.harvester.engine.utils.TriggerReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
  protected final HistoryManager historyManager;
  protected final TriggerInstanceManager triggerInstanceManager;
  protected final ExecutionService executionService;

  /**
   * Creates instance of the service.
   * @param triggerRegistry trigger registry
   * @param triggerManager trigger manager
   * @param historyManager history manager
   * @param triggerInstanceManager trigger instance manager
   * @param executionService execution service
   */
  public DefaultTriggersService(TriggerRegistry triggerRegistry, TriggerManager triggerManager, HistoryManager historyManager, TriggerInstanceManager triggerInstanceManager, ExecutionService executionService) {
    this.triggerRegistry = triggerRegistry;
    this.triggerManager = triggerManager;
    this.historyManager = historyManager;
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
  public Collection<Map.Entry<UUID, TriggerManager.TaskUuidTriggerDefinitionPair>> select() throws CrudlException {
    return triggerManager.list();
  }
  
  @Override
  public TriggerReference deactivateTriggerInstance(UUID triggerInstanceUuid) throws InvalidDefinitionException, DataProcessorException {
    TaskUuidTriggerInstancePair pair = triggerInstanceManager.remove(triggerInstanceUuid);
    if (pair == null) {
      throw new InvalidDefinitionException(formatForLog("Invalid trigger id: %s", triggerInstanceUuid));
    }
    try {
      TriggerDefinition trigDef = pair.getTriggerInstance().getTriggerDefinition();
      TriggerReference triggerReference = new TriggerReference(triggerInstanceUuid, pair.getTaskId(), trigDef);
      pair.getTriggerInstance().deactivate();
      return triggerReference;
    } catch (Exception ex) {
      throw new DataProcessorException(formatForLog("Error deactivating trigger: %s", triggerInstanceUuid), ex);
    } finally {
      try {
        triggerManager.delete(triggerInstanceUuid);
      } catch (CrudlException ex) {
        LOG.warn(formatForLog("Error deleting trigger: %s", triggerInstanceUuid), ex);
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
  public List<TriggerReference> listActivatedTriggers(UUID taskId) {
    return triggerInstanceManager.listAll().stream()
            .filter(e->taskId.equals(e.getValue().getTaskId()))
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
            throw new InvalidDefinitionException(formatForLog("Invalid trigger type: %s", definition.getTriggerDefinition().getType()));
          }
          TriggerInstance triggerInstance = trigger.createInstance(definition.getTriggerDefinition());
          
          TaskUuidTriggerInstancePair pair2 = new TriggerInstanceManager.TaskUuidTriggerInstancePair();
          pair2.setTaskId(definition.getTaskUuid());
          pair2.setTriggerInstance(triggerInstance);
          triggerInstanceManager.put(uuid, pair2);
          
          TriggerInstance.Context context = new TriggerContext(definition.getTaskUuid());
          triggerInstance.activate(context);
        } catch (DataProcessorException|InvalidDefinitionException ex) {
          LOG.warn(formatForLog("Error creating and activating trigger instance: %s -> %s", uuid, definition), ex);
        }
      });
    } catch (CrudlException ex) {
      LOG.error("Error processing trigger definitions", ex);
    }
  }
  
  @Override
  public void deactivateTriggerInstances() {
    triggerInstanceManager.listAll().stream().forEach(e->{
      TriggerInstance triggerInstance = e.getValue().getTriggerInstance();
      triggerInstance.deactivate();
    });
    triggerInstanceManager.clear();
  }
  
  @Override
  public TriggerReference schedule(UUID taskId, TriggerDefinition trigDef, InputBroker.IteratorContext iteratorContext) throws InvalidDefinitionException, DataProcessorException {
    try {
      TriggerManager.TaskUuidTriggerDefinitionPair pair = new TriggerManager.TaskUuidTriggerDefinitionPair();
      pair.setTaskUuid(taskId);
      pair.setTriggerDefinition(trigDef);
      UUID uuid = triggerManager.create(pair);
      Trigger trigger = triggerRegistry.get(trigDef.getType());
      TriggerInstance triggerInstance = trigger.createInstance(trigDef);
      
      TaskUuidTriggerInstancePair pair2 = new TriggerInstanceManager.TaskUuidTriggerInstancePair();
      pair2.setTaskId(taskId);
      pair2.setTriggerInstance(triggerInstance);
      triggerInstanceManager.put(uuid, pair2);
      
      TriggerContext context = new TriggerContext(taskId);
      triggerInstance.activate(context);
      return new TriggerReference(uuid, taskId, trigDef);
    } catch (CrudlException ex) {
      throw new DataProcessorException(formatForLog("Error scheduling task: %s", trigDef.getTaskDefinition()), ex);
    }
  }
  
  /**
   * DefaultEngine-bound trigger context.
   */
  private class TriggerContext implements TriggerInstance.Context {
    private final UUID taskId;
    
    /**
     * Creates instance of the context.
     * @param taskId task id
     */
    public TriggerContext(UUID taskId) {
      this.taskId = taskId;
    }

    @Override
    public synchronized ProcessInstance execute(TaskDefinition taskDefinition) throws DataProcessorException, InvalidDefinitionException {
      SimpleIteratorContext iteratorContext = new SimpleIteratorContext();
      iteratorContext.setLastHarvest(taskDefinition.isIncremental()? lastHarvest(): null);
      ProcessReference ref = executionService.execute(taskDefinition,iteratorContext);
      if (taskId!=null) {
        ref.getProcess().addListener(new HistoryManagerAdaptor(taskId, ref.getProcess(), historyManager));
      }
      ref.getProcess().init();
      return ref.getProcess();
    }
    
    @Override
    public Date lastHarvest() throws DataProcessorException {
      try {
        if (taskId!=null) {
          History history = historyManager.buildHistory(taskId);
          History.Event lastEvent = history!=null? history.getLastEvent(): null;
          return lastEvent!=null? lastEvent.getStartTimestamp(): null;
        } else {
          return null;
        }
      } catch (CrudlException ex) {
        throw new DataProcessorException(formatForLog("Error getting last harvest for: %s", taskId), ex);
      }
    }
  }
}
