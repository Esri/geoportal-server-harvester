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
package com.esri.geoportal.harvester.support;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.engine.utils.BrokerReference;
import com.esri.geoportal.harvester.engine.utils.BrokerReference.Category;
import java.util.UUID;

/**
 * Broker response.
 */
public final class BrokerResponse {
  private final UUID uuid;
  private final Category category;
  private final EntityDefinition brokerDefinition;

  /**
   * Creates instance of the broker response.
   * @param uuid broker uuid.
   * @param category broker category
   * @param brokerDefinition broker definition
   */
  public BrokerResponse(UUID uuid, Category category, EntityDefinition brokerDefinition) {
    this.uuid = uuid;
    this.category = category;
    this.brokerDefinition = brokerDefinition;
  }

  /**
   * Creates instance of the broker response.
   * @param brokerRef broker reference
   */
  private BrokerResponse(BrokerReference brokerRef) {
    this.uuid = brokerRef.getUuid();
    this.category = brokerRef.getCategory();
    this.brokerDefinition = brokerRef.getBrokerDefinition();
  }
  
  /**
   * Creates from broker reference.
   * @param brokerRef broker reference
   * @return broker response or <code>null</code> if no broker reference provided
   */
  public static BrokerResponse createFrom(BrokerReference brokerRef) {
    return brokerRef!=null? new BrokerResponse(brokerRef): null;
  }

  /**
   * Gets broker uuid.
   * @return broker uuid
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   * Gets broker category.
   * @return broker category
   */
  public Category getCategory() {
    return category;
  }

  /**
   * Gets broker definition.
   * @return broker definition
   */
  public EntityDefinition getBrokerDefinition() {
    return brokerDefinition;
  }
}
