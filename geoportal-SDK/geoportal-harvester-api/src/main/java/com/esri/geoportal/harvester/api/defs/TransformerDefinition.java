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
package com.esri.geoportal.harvester.api.defs;

import java.io.Serializable;
import java.util.Map;

/**
 * Transformer definition.
 */
public final class TransformerDefinition implements Serializable {
  private String type;
  private Map<String,String> properties;

  /**
   * Gets transformer type.
   * @return transformer type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets transformer type.
   * @param type transformer type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets properties.
   * @return properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Sets properties.
   * @param properties properties
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
  
  
  @Override
  public String toString() {
    return String.format("TRANSFORMER :: type: %s, arguments: %s", getType(), getProperties());
  }
  
}
