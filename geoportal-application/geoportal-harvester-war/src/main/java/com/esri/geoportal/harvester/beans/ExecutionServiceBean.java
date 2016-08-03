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

import com.esri.geoportal.harvester.engine.ProcessesService;
import com.esri.geoportal.harvester.engine.impl.DefaultExecutionService;
import com.esri.geoportal.harvester.engine.registers.FilterRegistry;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.registers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.registers.TransformerRegistry;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.registers.TriggerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Execution service bean.
 */
@Service
public class ExecutionServiceBean extends DefaultExecutionService {

  /**
   * Creates instance of the bean.
   * @param inboundConnectorRegistry inbound connector registry.
   * @param outboundConnectorRegistry outbound connector registry
   * @param transformerRegistry transformer registry
   * @param filterRegistry filter registry
   * @param processorRegistry processor registry
   * @param triggerRegistry trigger registry
   * @param triggerManager trigger manager
   * @param triggerInstanceManager trigger instance manager
   * @param historyManager history manager
   * @param processesService processes service
   */
  @Autowired
  public ExecutionServiceBean(
          InboundConnectorRegistry inboundConnectorRegistry, 
          OutboundConnectorRegistry outboundConnectorRegistry, 
          TransformerRegistry transformerRegistry,
          FilterRegistry filterRegistry,
          ProcessorRegistry processorRegistry, 
          TriggerRegistry triggerRegistry, 
          TriggerManager triggerManager, 
          TriggerInstanceManager triggerInstanceManager, 
          HistoryManager historyManager, 
          ProcessesService processesService) {
    super(inboundConnectorRegistry, outboundConnectorRegistry, transformerRegistry, filterRegistry, processorRegistry, triggerRegistry, triggerManager, triggerInstanceManager, historyManager, processesService);
  }
  
}
