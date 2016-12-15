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
package com.esri.geoportal.harvester.api.base;

import com.esri.geoportal.harvester.api.Broker;
import com.esri.geoportal.harvester.api.ProcessInstance.Listener;
import com.esri.geoportal.harvester.api.defs.Task;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Simple broker context.
 */
public class SimpleInitContext implements Broker.InitContext {
  private final Task task;
  private final List<Listener> listeners;

  /**
   * Creates instance of the context.
   * @param task task.
   * @param listeners listeners
   */
  public SimpleInitContext(Task task, List<Listener> listeners) {
    this.task = task;
    this.listeners = listeners;
  }

  @Override
  public Task getTask() {
    return task;
  }

  @Override
  public Map<String, String> getParams() {
    return Collections.emptyMap();
  }

  @Override
  public void addListener(Listener listener) {
    listeners.add(listener);
  }
  
}
