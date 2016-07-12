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
import java.util.stream.Collectors;

/**
 * Channel link definition.
 */
public final class LinkDefinition {
  private EntityDefinition action;
  private List<LinkDefinition> drains;

  /**
   * Gets link action.
   * @return link action
   */
  public EntityDefinition getAction() {
    return action;
  }

  /**
   * Sets link action.
   * @param action link action
   */
  public void setAction(EntityDefinition action) {
    this.action = action;
  }

  /**
   * Gets drains.
   * @return drains
   */
  public List<LinkDefinition> getDrains() {
    return drains;
  }

  /**
   * Sets drains.
   * @param drains drains
   */
  public void setDrains(List<LinkDefinition> drains) {
    this.drains = drains;
  }
  
  @Override
  public String toString() {
    return String.format("LINK %s:[%s]", action, drains.stream().map(d->d.toString()).collect(Collectors.joining(",")));
  }
}
