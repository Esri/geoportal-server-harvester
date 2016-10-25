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
package com.esri.geoportal.cli.boot;

import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * In-memory trigger instance manager.
 */
public class MemTriggerInstanceManager implements TriggerInstanceManager {
  private final HashMap<UUID,TaskUuidTriggerInstancePair> mem = new HashMap<>();

  @Override
  public void put(UUID uuid, TaskUuidTriggerInstancePair instance) {
    mem.put(uuid, instance);
  }

  @Override
  public TaskUuidTriggerInstancePair get(UUID uuid) {
    return mem.get(uuid);
  }

  @Override
  public TaskUuidTriggerInstancePair remove(UUID uuid) {
    return mem.remove(uuid);
  }

  @Override
  public List<Map.Entry<UUID, TaskUuidTriggerInstancePair>> listAll() {
    return mem.entrySet().stream().collect(Collectors.toList());
  }

  @Override
  public void clear() {
    mem.clear();
  }
  
}
