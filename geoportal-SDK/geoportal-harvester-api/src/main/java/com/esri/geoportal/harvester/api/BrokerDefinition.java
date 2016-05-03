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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Broker definition.
 * <p>
 * It is a map of properties mean to be serializable. Based upon this information,
 * a {@link Connector} will be able to produce an instance of the {@link Broker}.
 * <p>
 * This class must be extended with the class providing explicit methods translating
 * from/to the named attribute within the map. For example, a concretized class
 * may provide a pair of getter and setter: getHostUrl() and setHostUrl() which 
 * return or accept an argument of type URL. This value will be serialized into 
 * string and stored within the map under predetermined key.
 * 
 * @see Connector
 * @see Broker
 */
public final class BrokerDefinition {
  private String type;
  private Map<String,String> properties = new LinkedHashMap<>();

  /**
   * Gets broker type.
   * @return broker type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets broker type.
   * @param type broker type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets broker properties.
   * @return broker properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Sets broker properties.
   * @param properties broker properties
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = properties!=null? properties: new LinkedHashMap<>();
  }
}
