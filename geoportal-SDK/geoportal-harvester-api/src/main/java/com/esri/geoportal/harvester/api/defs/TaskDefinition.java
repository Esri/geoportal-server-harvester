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

import java.util.List;
import java.util.Objects;

/**
 * Task definition.
 */
public final class TaskDefinition {
  private EntityDefinition processor;
  private EntityDefinition source;
  private List<EntityDefinition> destinations;

  /**
   * Gets processor definition.
   * @return processor definition or <code>null</code> if default processor shall be used
   */
  public EntityDefinition getProcessor() {
    return processor;
  }

  /**
   * Sets processor definition.
   * @param processor processor definition or <code>null</code> for default processor
   */
  public void setProcessor(EntityDefinition processor) {
    this.processor = processor;
  }

  /**
   * Gets source definition.
   * @return source definition
   */
  public EntityDefinition getSource() {
    return source;
  }

  /**
   * Sets source definition.
   * @param source source definition
   */
  public void setSource(EntityDefinition source) {
    this.source = source;
  }

  /**
   * Gets destinations.
   * @return destinations
   */
  public List<EntityDefinition> getDestinations() {
    return destinations;
  }

  /**
   * Sets destinations.
   * @param destinations destinations
   */
  public void setDestinations(List<EntityDefinition> destinations) {
    this.destinations = destinations;
  }
  
  @Override
  public String toString() {
    return String.format("PROCESSOR: %s, SOURCE: %s, DESTINATIONS: %s", processor, source, destinations!=null? destinations: null);
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof TaskDefinition) {
      TaskDefinition td = (TaskDefinition)o;
      return ((getProcessor()!=null && td.getProcessor()!=null && getProcessor().equals(td.getProcessor())) || (getProcessor()==null && td.getProcessor()==null)) &&
             ((getSource()!=null && td.getSource()!=null && getSource().equals(td.getSource())) || (getSource()==null && td.getSource()==null)) &&
             ((getDestinations()!=null && td.getDestinations()!=null && getDestinations().equals(td.getDestinations())) || (getDestinations()==null && td.getDestinations()==null)) ;
    }
    
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 79 * hash + Objects.hashCode(this.processor);
    hash = 79 * hash + Objects.hashCode(this.source);
    hash = 79 * hash + Objects.hashCode(this.destinations);
    return hash;
  }
}
