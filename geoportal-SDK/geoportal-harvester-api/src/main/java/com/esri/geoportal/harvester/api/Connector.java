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

/**
 * Connector.
 * @param <B> broker type.
 * @param <D> broker definition type
 * @see Broker
 * @see BrokerDefinition
 */
public interface Connector<B extends Broker, D extends BrokerDefinition> {
  /**
   * Gets connector UI template.
   * @return connector UI template
   */
  ConnectorTemplate getTemplate();
  
  /**
   * Creates new broker instance based on broker definition.
   * @param definition broker definition
   * @return broker instance
   * @throws InvalidDefinitionException if provided definition is invalid
   */
  B createBroker(D definition) throws InvalidDefinitionException;
}
