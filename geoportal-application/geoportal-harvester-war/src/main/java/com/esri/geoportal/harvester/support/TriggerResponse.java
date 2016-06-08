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

import com.esri.geoportal.harvester.api.defs.TriggerInstanceDefinition;
import java.util.UUID;

/**
 * Trigger response.
 */
public final class TriggerResponse {
  private final UUID uuid;
  private final TriggerInstanceDefinition triggerDefinition;

  /**
   * Creates instance of the trigger info.
   * @param uuid uuid
   * @param triggerDefinition trigger definition
   */
  public TriggerResponse(UUID uuid, TriggerInstanceDefinition triggerDefinition) {
    this.uuid = uuid;
    this.triggerDefinition = triggerDefinition;
  }

  /**
   * Gets trigger instance UUID.
   * @return trigger instance UUID
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   * Gets trigger definition.
   * @return trigger definition
   */
  public TriggerInstanceDefinition getTriggerDefinition() {
    return triggerDefinition;
  }
  
  @Override
  public String toString() {
    return String.format("TRIGGER INFO :: uuid: %s, trigger definition: %s", uuid, triggerDefinition);
  }
}
