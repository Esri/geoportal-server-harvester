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

import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.engine.services.TemplatesService;
import com.esri.geoportal.harvester.engine.registers.FilterRegistry;
import com.esri.geoportal.harvester.engine.registers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.registers.TransformerRegistry;
import com.esri.geoportal.harvester.engine.registers.TriggerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Default templates service.
 */
public class DefaultTemplatesService implements TemplatesService {
  protected final InboundConnectorRegistry inboundConnectorRegistry;
  protected final OutboundConnectorRegistry outboundConnectorRegistry;
  protected final TransformerRegistry transformerRegistry;
  protected final FilterRegistry filterRegistry;
  protected final TriggerRegistry triggerRegistry;
  protected final ProcessorRegistry processorRegistry;

  /**
   * Creates instance of the service.
   * @param inboundConnectorRegistry inbound connector registry
   * @param outboundConnectorRegistry outbound connector registry
   * @param transformerRegistry transformer registry
   * @param filterRegistry filter registry
   * @param triggerRegistry trigger registry
   * @param processorRegistry processor registry
   */
  public DefaultTemplatesService(
          InboundConnectorRegistry inboundConnectorRegistry, 
          OutboundConnectorRegistry outboundConnectorRegistry, 
          TransformerRegistry transformerRegistry,
          FilterRegistry filterRegistry,
          TriggerRegistry triggerRegistry, 
          ProcessorRegistry processorRegistry) {
    this.inboundConnectorRegistry = inboundConnectorRegistry;
    this.outboundConnectorRegistry = outboundConnectorRegistry;
    this.transformerRegistry = transformerRegistry;
    this.filterRegistry = filterRegistry;
    this.triggerRegistry = triggerRegistry;
    this.processorRegistry = processorRegistry;
  }
  
  @Override
  public List<UITemplate> getInboundConnectorTemplates(Locale locale) {
    return inboundConnectorRegistry.getTemplates(locale);
  }

  @Override
  public List<UITemplate> getOutboundConnectorTemplates(Locale locale) {
    return outboundConnectorRegistry.getTemplates(locale);
  }

  @Override
  public List<UITemplate> getTransformerTemplates(Locale locale) {
    return transformerRegistry.getTemplates(locale);
  }

  @Override
  public List<UITemplate> getFilterTemplates(Locale locale) {
    return filterRegistry.getTemplates(locale);
  }

  @Override
  public List<UITemplate> getTriggersTemplates(Locale locale) {
    return triggerRegistry.values().stream().map(v->v.getTemplate(locale)).collect(Collectors.toList());
  }

  @Override
  public List<UITemplate> getProcessorsTemplates(Locale locale) {
    List<UITemplate> templates = new ArrayList<>();
    templates.add(processorRegistry.getDefaultProcessor().getTemplate(locale));
    templates.addAll(processorRegistry.values().stream().map(p->p.getTemplate(locale)).collect(Collectors.toList()));
    return templates;
  }
}
