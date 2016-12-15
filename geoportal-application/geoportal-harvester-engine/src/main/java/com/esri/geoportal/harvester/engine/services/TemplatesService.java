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

import com.esri.geoportal.harvester.api.defs.UITemplate;
import java.util.List;
import java.util.Locale;

/**
 * Templates service.
 */
public interface TemplatesService {

  /**
   * Gets inbound connector templates.
   *
   * @param locale locale
   * @return list of inbound connector templates
   */
  List<UITemplate> getInboundConnectorTemplates(Locale locale);
  
  /**
   * Gets outbound connector templates.
   *
   * @param locale locale
   * @return list of outbound connector templates
   */
  List<UITemplate> getOutboundConnectorTemplates(Locale locale);

  /**
   * Gets transformer templates.
   * 
   * @param locale locale
   * @return transformer templates
   */
  List<UITemplate> getTransformerTemplates(Locale locale);
  
  /**
   * Gets filter templates.
   * 
   * @param locale locale
   * @return filter templates
   */
  List<UITemplate> getFilterTemplates(Locale locale);
  
  /**
   * Gets triggers templates.
   * 
   * @param locale locale
   * @return list of trigger templates
   */
  List<UITemplate> getTriggersTemplates(Locale locale);

  /**
   * Gets processors templates.
   * 
   * @param locale locale
   * @return list of processor templates
   */
  List<UITemplate> getProcessorsTemplates(Locale locale);
}
