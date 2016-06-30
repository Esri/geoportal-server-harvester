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

import com.esri.geoportal.harvester.api.TriggerInstance;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trigger instance manager.
 */
public interface TriggerInstanceManager {
  
  /**
   * Puts trigger instance into the manager.
   * @param uuid uuid of the trigger instance
   * @param instance trigger instance
   */
  void put(UUID uuid, TriggerInstance instance);
  
  /**
   * Gets a trigger instance from the manager.
   * @param uuid uuid of the trigger instance
   * @return trigger instance
   */
  TriggerInstance get(UUID uuid);
  
  /**
   * Removes trigger instance.
   * @param uuid uuid of the trigger instance
   * @return removed instance
   */
  TriggerInstance remove(UUID uuid);
  
  /**
   * Lists all instances.
   * @return list of instance entries.
   */
  List<Map.Entry<UUID, TriggerInstance>> listAll();
  
  /**
   * Clears manager.
   */
  void clear();
}
