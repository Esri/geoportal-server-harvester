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

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.engine.support.BrokerReference;
import java.util.List;
import java.util.UUID;

/**
 * Brokers service.
 */
public interface BrokersService {
  
  /**
   * Creates a broker.
   *
   * @param brokerDefinition broker definition
   * @return broker info or <code>null</code> if broker has not been created
   * @throws DataProcessorException if accessing repository fails
   */
  BrokerReference createBroker(EntityDefinition brokerDefinition) throws DataProcessorException;

  /**
   * Creates a broker.
   *
   * @param brokerId broker id
   * @param brokerDefinition broker definition
   * @return broker info or <code>null</code> if broker has not been created
   * @throws DataProcessorException if accessing repository fails
   */
  BrokerReference updateBroker(UUID brokerId, EntityDefinition brokerDefinition) throws DataProcessorException;

  /**
   * Deletes broker.
   *
   * @param brokerId broker id
   * @return <code>true</code> if broker has been deleted
   * @throws DataProcessorException if accessing repository fails
   */
  boolean deleteBroker(UUID brokerId) throws DataProcessorException;

  /**
   * Finds broker by id.
   *
   * @param brokerId broker id
   * @return broker info or <code>null</code> if no broker corresponding to the
   * broker id can be found
   * @throws DataProcessorException if accessing repository fails
   */
  BrokerReference findBroker(UUID brokerId) throws DataProcessorException;

  /**
   * Gets broker definitions.
   *
   * @param category broker category
   * @return broker infos
   * @throws DataProcessorException if accessing repository fails
   */
  List<BrokerReference> getBrokersDefinitions(BrokerReference.Category category) throws DataProcessorException;

}
