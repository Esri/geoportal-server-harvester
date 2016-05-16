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
package com.esri.geoportal.harvester.engine;

import com.esri.geoportal.harvester.api.ProcessHandle;
import java.util.UUID;

/**
 * Process wrapper.
 */
public class ProcessWrapper implements ProcessHandle {
  private final UUID uuid;
  private final ProcessHandle processHandle;

  /**
   * Creates instance of the wrapper.
   * @param uuid id of the process
   * @param processHandle process handle
   */
  public ProcessWrapper(UUID uuid, ProcessHandle processHandle) {
    this.uuid = uuid;
    this.processHandle = processHandle;
  }
  
  @Override
  public String toString() {
    return String.format("%s, uuid: %s", processHandle, uuid);
  }

  @Override
  public UUID getProcessId() {
    return uuid;
  }

  @Override
  public String getTitle() {
    return processHandle.getTitle();
  }

  @Override
  public void begin() {
    processHandle.begin();
  }

  @Override
  public void abort() {
    processHandle.abort();
  }

  @Override
  public Status getStatus() {
    return processHandle.getStatus();
  }

  @Override
  public void addListener(Listener listener) {
    processHandle.addListener(listener);
  }
}
