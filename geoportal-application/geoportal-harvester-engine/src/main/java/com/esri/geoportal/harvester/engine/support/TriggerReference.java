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
  private final UUID taskId;
  private final TriggerDefinition triggerDefinition;

  /**
   * Creates instance of the reference.
   * @param uuid trigger uuid
   * @param taskId task id
   * @param triggerDefinition trigger instance definition 
   */
  public TriggerReference(UUID uuid, UUID taskId, TriggerDefinition triggerDefinition) {
    this.uuid = uuid;
    this.taskId = taskId;
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
   * Gets task id if any.
   * @return task id
   */
  public UUID getTaskId() {
    return taskId;
  }

  /**
   * Gets trigger instance definition.
   * @return trigger instance definition
   */
  public TriggerDefinition getTriggerDefinition() {
    return triggerDefinition;
  }
  
  @Override
  public String toString() {
    return String.format("TRIGGER REF :: uuid: %s, taskId: %s, definition: %s", uuid, taskId, triggerDefinition);
  }
}
