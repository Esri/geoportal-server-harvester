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

import java.util.Map;
import java.util.Objects;

/**
 * Trigger definition.
 */
public final class TriggerInstanceDefinition {
  private String type;
  private TaskDefinition taskDefinition;
  private Map<String,String> properties;

  /**
   * Gets trigger type.
   * @return trigger type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets trigger type.
   * @param type trigger type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets task definition.
   * @return task definition
   */
  public TaskDefinition getTaskDefinition() {
    return taskDefinition;
  }

  /**
   * Sets task definition.
   * @param taskDefinition task definition 
   */
  public void setTaskDefinition(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
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
    return String.format("TRIGGER :: type: %s, task definition: %s, arguments: %s", getType(), getTaskDefinition(), getProperties());
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof TriggerInstanceDefinition) {
      TriggerInstanceDefinition t = (TriggerInstanceDefinition) o;
      return ((getType()!=null && t.getType()!=null && getType().equals(t.getType())) || (getType()==null && t.getType()==null)) &&
             ((getTaskDefinition()!=null && t.getTaskDefinition()!=null && getTaskDefinition().equals(t.getTaskDefinition())) || (getTaskDefinition()==null && t.getTaskDefinition()==null)) &&
              ((getProperties()!=null && t.getProperties()!=null && getProperties().equals(t.getProperties())) || (getProperties()==null && t.getProperties()==null)); 
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + Objects.hashCode(this.type);
    hash = 59 * hash + Objects.hashCode(this.taskDefinition);
    hash = 59 * hash + Objects.hashCode(this.properties);
    return hash;
  }
}
