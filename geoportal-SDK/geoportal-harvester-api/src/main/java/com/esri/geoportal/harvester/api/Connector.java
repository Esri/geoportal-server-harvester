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
package com.esri.geoportal.harvester.api;

import com.esri.geoportal.harvester.api.general.Entity;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;

/**
 * Connector.
 * <p>
 * It is a factory interface providing methods to create an instance of the
 * {@link Broker} and create instance of the {@link UITemplate}.
 * 
 * @param <B> type of the broker
 * 
 * @see com.esri.geoportal.harvester.api API
 * @see com.esri.geoportal.harvester.api.specs.InputConnector
 * @see com.esri.geoportal.harvester.api.specs.OutputConnector
 * @see Broker
 * @see EntityDefinition
 */
public interface Connector<B extends Broker> extends Entity {
  
  /**
   * Validates broker definition.
   * @param definition broker definition
   * @throws InvalidDefinitionException if broker definition is invalid
   */
  void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException;
  
  /**
   * Creates new broker instance based on broker definition.
   * @param definition broker definition
   * @return broker instance
   * @throws InvalidDefinitionException if provided definition is invalid
   */
  B createBroker(EntityDefinition definition) throws InvalidDefinitionException;
  
  /**
   * Gets resource locator.
   * @param definition connector definition
   * @return resource locator (URL, URI, JNDI, etc.)
   */
  String getResourceLocator(EntityDefinition definition);
}
