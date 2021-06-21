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
package com.esri.geoportal.harvester.engine.utils;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import java.util.UUID;

/**
 * Brokers information.
 */
public final class BrokerReference {
  private final UUID uuid;
  private final Category category;
  private final EntityDefinition brokerDefinition;
  private final String resourceLocator;

  /**
   * Creates instance of the broker info.
   * @param uuid broker uuid.
   * @param category broker category
   * @param brokerDefinition broker definition
   */
  public BrokerReference(UUID uuid, Category category, EntityDefinition brokerDefinition, String resourceLocator) {
    this.uuid = uuid;
    this.category = category;
    this.brokerDefinition = brokerDefinition;
    this.resourceLocator = resourceLocator;
  }

  /**
   * Gets resource locator.
   * @return resource locator
   */
  public String getResourceLocator() {
    return resourceLocator;
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
  
  @Override
  public String toString() {
    return String.format("BROKER REF :: uuid: %s, category: %s, definition: %s", uuid, category, brokerDefinition);
  }
  
  /**
   * Broker category.
   */
  public static enum Category {
    /** inbound broker */
    INBOUND, 
    /** outbound broker */
    OUTBOUND;
    
    /**
     * Parses category given as string (case insensitive).
     * @param strCateg category as string
     * @return category
     * @throws IllegalArgumentException if invalid input.
     */
    public static Category parse(String strCateg) {
      for (Category c: values()) {
        if (c.name().equalsIgnoreCase(strCateg)) {
          return c;
        }
      }
      throw new IllegalArgumentException(String.format("Invalid category: %s", strCateg));
    }
  }
}
