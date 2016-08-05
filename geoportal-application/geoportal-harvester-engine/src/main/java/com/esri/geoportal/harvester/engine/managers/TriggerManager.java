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
package com.esri.geoportal.harvester.engine.managers;

import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.engine.managers.TriggerManager.TaskUuidTriggerDefinitionPair;
import com.esri.geoportal.harvester.engine.support.CrudsRepo;
import java.util.UUID;

/**
 * Trigger manager.
 */
public interface TriggerManager extends CrudsRepo<TaskUuidTriggerDefinitionPair> {
  
  public static final class TaskUuidTriggerDefinitionPair {
    private UUID taskUuid;
    private TriggerDefinition triggerDefinition;

    /**
     * Gets task id.
     * @return the taskUuid
     */
    public UUID getTaskUuid() {
      return taskUuid;
    }

    /**
     * Sets task id.
     * @param taskUuid the taskUuid to set
     */
    public void setTaskUuid(UUID taskUuid) {
      this.taskUuid = taskUuid;
    }

    /**
     * Gets trigger definition.
     * @return the triggerDefinition
     */
    public TriggerDefinition getTriggerDefinition() {
      return triggerDefinition;
    }

    /**
     * Sets trigger definition.
     * @param triggerDefinition the triggerDefinition to set
     */
    public void setTriggerDefinition(TriggerDefinition triggerDefinition) {
      this.triggerDefinition = triggerDefinition;
    }
  }
}
