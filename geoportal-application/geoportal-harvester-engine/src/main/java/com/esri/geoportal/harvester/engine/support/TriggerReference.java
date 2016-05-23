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
package com.esri.geoportal.harvester.engine.support;

import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import java.util.UUID;

/**
 * Trigger reference.
 */
public final class TriggerReference {
  private final UUID uuid;
  private final TriggerDefinition triggerDefinition;

  /**
   * Creates instance of the reference.
   * @param uuid trigger uuid
   * @param triggerDefinition trigger definition 
   */
  public TriggerReference(UUID uuid, TriggerDefinition triggerDefinition) {
    this.uuid = uuid;
    this.triggerDefinition = triggerDefinition;
  }

  /**
   * Gets trigger uuid.
   * @return trigger uuid
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   * Gets trigger definition.
   * @return trigger definition
   */
  public TriggerDefinition getTriggerDefinition() {
    return triggerDefinition;
  }
  
  @Override
  public String toString() {
    return String.format("TRIGGER REFERENCE :: uuid: %s, definition: %s", uuid, triggerDefinition);
  }
}
