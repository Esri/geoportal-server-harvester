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
package com.esri.geoportal.harvester.engine.registers;

import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.engine.utils.EntityRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trigger registry.
 */
public class TriggerRegistry extends EntityRegistry<Trigger> implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(TriggerRegistry.class);

  @Override
  public void close() throws Exception {
    values().stream().forEach(trigger->{
      try {
        trigger.close();
      } catch (Exception ex) {
        LOG.warn(String.format("Error closing trigger: %s", trigger.getType()), ex);
      }
    });
  }
}
