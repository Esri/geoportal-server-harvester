/*
 * Copyright 2016 Esri, Inc..
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

import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import java.util.UUID;

/**
 * Task info.
 */
public final class TaskInfo {
  private final UUID uuid;
  private final TaskDefinition taskDefinition;

  /**
   * Creates instance of the task info.
   * @param uuid  uuid of the task info
   * @param taskDefinition task definition
   */
  public TaskInfo(UUID uuid, TaskDefinition taskDefinition) {
    this.uuid = uuid;
    this.taskDefinition = taskDefinition;
  }

  /**
   * Gets uuid of the task info.
   * @return uuid
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   * Gets task definition.
   * @return task definition
   */
  public TaskDefinition getTaskDefinition() {
    return taskDefinition;
  }
  
  @Override
  public String toString() {
    return String.format("id: %s, taskDefinition: %s", uuid, taskDefinition);
  }
}
