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

import com.esri.geoportal.harvester.engine.impl.DefaultTemplatesService;
import com.esri.geoportal.harvester.engine.managers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.managers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.managers.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.managers.TriggerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Templates service bean.
 */
@Service
public class TemplatesServiceBean extends DefaultTemplatesService {

  /**
   * Creates instance of the bean.
   * @param inboundConnectorRegistry inbound connector registry
   * @param outboundConnectorRegistry outbound connector registry
   * @param triggerRegistry trigger registry
   * @param processorRegistry processor registry
   */
  @Autowired
  public TemplatesServiceBean(InboundConnectorRegistry inboundConnectorRegistry, OutboundConnectorRegistry outboundConnectorRegistry, TriggerRegistry triggerRegistry, ProcessorRegistry processorRegistry) {
    super(inboundConnectorRegistry, outboundConnectorRegistry, triggerRegistry, processorRegistry);
  }
  
}
