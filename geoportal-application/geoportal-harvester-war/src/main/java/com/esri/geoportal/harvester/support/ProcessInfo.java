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

import com.esri.geoportal.harvester.api.IProcess.Status;
import java.util.UUID;

/**
 * Process info.
 */
public final class ProcessInfo {
  private final UUID uuid;
  private final String description;
  private final Status status;

  /**
   * Creates instance of the process info.
   * @param uuid process uuid
   * @param description process description
   * @param status process status
   */
  public ProcessInfo(UUID uuid, String description, Status status) {
    this.uuid = uuid;
    this.description = description;
    this.status = status;
  }

  /**
   * Gets process uuid.
   * @return process uuid
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   * Gets process description.
   * @return process description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets status.
   * @return status
   */
  public Status getStatus() {
    return status;
  }
  
  @Override
  public String toString() {
    return String.format("PROCESS :: id: %s, description: %s, status: %s", uuid, description, status);
  }
  
}
