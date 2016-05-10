/*
 * Copyright 2016 Esri, Inc
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
package com.esri.geoportal.harvester.console;

import com.esri.geoportal.harvester.api.EntityDefinition;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import org.apache.commons.lang3.StringUtils;

/**
 * Console broker definition adaptor.
 */
public class ConsoleBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {

  /**
   * Creates instance of the adaptor.
   * @param def broker definition
   */
  public ConsoleBrokerDefinitionAdaptor(EntityDefinition def) {
    super(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(ConsoleConnector.TYPE);
    } else if (!ConsoleConnector.TYPE.equals(def.getType())) {
      throw new IllegalArgumentException("Broker definition doesn't match");
    }
  }
}
