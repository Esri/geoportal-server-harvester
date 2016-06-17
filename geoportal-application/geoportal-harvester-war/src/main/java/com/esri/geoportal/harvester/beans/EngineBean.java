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
package com.esri.geoportal.harvester.beans;

import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.defs.TriggerInstanceDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.managers.BrokerDefinitionManager;
import com.esri.geoportal.harvester.engine.managers.ReportBuilder;
import com.esri.geoportal.harvester.engine.impl.DefaultEngine;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.managers.ProcessManager;
import com.esri.geoportal.harvester.engine.managers.TaskManager;
import com.esri.geoportal.harvester.engine.managers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.managers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.managers.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.managers.TriggerRegistry;
import com.esri.geoportal.harvester.engine.support.CrudsException;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DefaultEngine bean.
 */
@Service
public class EngineBean extends DefaultEngine {
  private static final Logger LOG = LoggerFactory.getLogger(EngineBean.class);

  /**
   * Creates instance of the engine bean.
   * @param inboundConnectorRegistry inbound connector registry
   * @param outboundConnectorRegistry outbound connector registry
   * @param triggerRegistry trigger registry
   * @param processorRegistry processor registry
   * @param brokerDefinitionManager broker definition manager
   * @param taskManager task manager
   * @param processManager process manager
   * @param triggerManager trigger manager
   * @param triggerInstanceManager trigger instance manager
   * @param historyManager history manager
   * @param reportBuilder report builder
   */
  @Autowired
  public EngineBean(
          InboundConnectorRegistry inboundConnectorRegistry, 
          OutboundConnectorRegistry outboundConnectorRegistry, 
          TriggerRegistry triggerRegistry,
          ProcessorRegistry processorRegistry,
          BrokerDefinitionManager brokerDefinitionManager, 
          TaskManager taskManager, 
          ProcessManager processManager, 
          TriggerManager triggerManager,
          TriggerInstanceManager triggerInstanceManager,
          HistoryManager historyManager,
          ReportBuilder reportBuilder
  ) {
    super(
            inboundConnectorRegistry, 
            outboundConnectorRegistry, 
            triggerRegistry, 
            processorRegistry,
            brokerDefinitionManager, 
            taskManager, 
            processManager, 
            triggerManager, 
            triggerInstanceManager,
            historyManager,
            reportBuilder
    );
  }
  
  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    activateTriggerInstances();
    LOG.info("EngineBean initialized.");
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    deactivateTriggerInstances();
    LOG.info(String.format("EngineBean destroyed."));
  }
  
  /**
   * Activates trigger instances
   */
  protected void activateTriggerInstances() {
    try {
      triggerManager.select().forEach(e->{
        UUID uuid = e.getKey();
        TriggerInstanceDefinition definition = e.getValue();
        
        try {
          Trigger trigger = triggerRegistry.get(definition.getType());
          if (trigger==null) {
            throw new InvalidDefinitionException(String.format("Invalid trigger type: %s", definition.getType()));
          }
          Trigger.Instance triggerInstance = trigger.createInstance(definition);
          Trigger.Context context = new TriggerContext(uuid,triggerInstance);
          triggerInstance.activate(context);
        } catch (DataProcessorException|InvalidDefinitionException ex) {
          LOG.warn(String.format("Error creating and activating trigger instance: %s -> %s", uuid, definition), ex);
        }
      });
    } catch (CrudsException ex) {
      LOG.error("Error processing trigger definitions", ex);
    }
  }
  
  /**
   * Deactivates trigger instances.
   */
  protected void deactivateTriggerInstances() {
    triggerInstanceManager.listAll().stream().forEach(e->{
      UUID uuid = e.getKey();
      Trigger.Instance triggerInstance = e.getValue();
      
      try {
        triggerInstance.close();
      } catch (Exception ex) {
        LOG.warn(String.format("Error deactivating trigger instance: %s --> %s", uuid, triggerInstance.getTriggerDefinition()), ex);
      }
    });
    triggerInstanceManager.clear();
  }
}
