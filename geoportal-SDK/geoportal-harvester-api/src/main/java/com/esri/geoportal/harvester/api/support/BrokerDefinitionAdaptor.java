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
package com.esri.geoportal.harvester.api.support;

import com.esri.geoportal.harvester.api.BrokerDefinition;

/**
 * Broker adaptor.
 */
public abstract class BrokerDefinitionAdaptor {
  
  private final BrokerDefinition def;

  /**
   * Creates instance of the adaptor.
   * @param def broker definition
   */
  public BrokerDefinitionAdaptor(BrokerDefinition def) {
    this.def = def;
  }
  
  /**
   * Gets property value.
   * @param propertyName property name
   * @return property value
   */
  protected final String get(String propertyName) {
    return def.getProperties().get(propertyName);
  }
  
  /**
   * Sets property value.
   * <p>
   * If property value is <code>null</code> then property is being removed from 
   * the properties of the definition.
   * @param propertyName property name
   * @param propertyValue property value
   */
  protected final void set(String propertyName, String propertyValue) {
    if (propertyValue!=null) {
      def.getProperties().put(propertyName, propertyValue);
    } else {
      def.getProperties().remove(propertyName);
    }
  }
}
