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
package com.esri.geoportal.harvester.engine;

import com.esri.geoportal.harvester.api.defs.UITemplate;
import java.util.List;

/**
 * Templates service.
 */
public interface TemplatesService {

  /**
   * Gets inbound connector templates.
   *
   * @return list of inbound connector templates
   */
  List<UITemplate> getInboundConnectorTemplates();

  /**
   * Gets triggers templates.
   * @return list of trigger templates
   */
  List<UITemplate> getTriggersRegistry();
  
  /**
   * Gets outbound connector templates.
   *
   * @return list of outbound connector templates
   */
  List<UITemplate> getOutboundConnectorTemplates();

  /**
   * Gets processors templates.
   * @return list of processor templates
   */
  List<UITemplate> getProcessorsTemplates();
}
