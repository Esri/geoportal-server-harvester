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

import com.esri.geoportal.harvester.engine.BrokerDefinitionManager;
import com.esri.geoportal.harvester.engine.ReportBuilder;
import com.esri.geoportal.harvester.engine.Engine;
import com.esri.geoportal.harvester.engine.HistoryManager;
import com.esri.geoportal.harvester.engine.ProcessManager;
import com.esri.geoportal.harvester.engine.TaskManager;
import com.esri.geoportal.harvester.engine.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.TriggerManager;
import com.esri.geoportal.harvester.engine.TriggerRegistry;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Engine bean.
 */
@Service
public class EngineBean extends Engine {
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
            historyManager,
            reportBuilder
    );
  }
  
  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    fireTriggers();
    LOG.info("EngineBean initialized.");
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    LOG.info(String.format("EngineBean destroyed."));
  }
}
