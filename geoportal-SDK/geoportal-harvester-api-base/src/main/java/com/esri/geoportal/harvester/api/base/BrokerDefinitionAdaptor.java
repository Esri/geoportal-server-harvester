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
package com.esri.geoportal.harvester.api.base;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import java.util.Map;

/**
 * Broker adaptor.
 */
public abstract class BrokerDefinitionAdaptor {
  
  private final EntityDefinition entityDefinition;

  /**
   * Creates instance of the adaptor.
   * @param entityDefinition broker definition
   */
  public BrokerDefinitionAdaptor(EntityDefinition entityDefinition) {
    this.entityDefinition = entityDefinition;
  }

  /**
   * Gets entity definition.
   * @return entity definition
   */
  public EntityDefinition getEntityDefinition() {
    return entityDefinition;
  }
  
  /**
   * Allows to override/update params.
   * @param params 
   */
  public abstract void override(Map<String,String> params);
  
  /**
   * Consumes parameter.
   * <p>
   * Uses given parameter to update existing parameter and removes given
   * parameter from the map.
   * @param params map of parameters
   * @param paramName parameter name
   */
  protected void consume(Map<String,String> params, String paramName) {
    if (params.containsKey(paramName)) {
      String value = params.get(paramName);
      set(paramName,value);
    }
  }
  
  /**
   * Gets property value.
   * @param propertyName property name
   * @return property value
   */
  protected final String get(String propertyName) {
    return entityDefinition.getProperties().get(propertyName);
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
      entityDefinition.getProperties().put(propertyName, propertyValue);
    } else {
      entityDefinition.getProperties().remove(propertyName);
    }
  }
  
  @Override
  public String toString() {
    return entityDefinition.toString();
  }
}
