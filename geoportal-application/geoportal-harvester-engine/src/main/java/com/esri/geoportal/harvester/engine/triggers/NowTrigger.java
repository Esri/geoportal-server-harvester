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
package com.esri.geoportal.harvester.engine.triggers;

import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;

/**
 * 'Now' trigger. Triggers harvesting immediately.
 */
public class NowTrigger implements Trigger {
  public static final String TYPE = "NOW";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate() {
    return new UITemplate(getType(), "Harvest now", null);
  }

  @Override
  public Instance createInstance(TriggerDefinition triggerDefinition) throws InvalidDefinitionException {
    if (!getType().equals(triggerDefinition.getType())) {
      throw new InvalidDefinitionException(String.format("Invalid trigger definition: %s", triggerDefinition));
    }
    return new NowTriggerInstance(triggerDefinition);
  }

  @Override
  public void close() throws Exception {
    // Ignore
  }
  
  /**
   * 'Now' trigger instance.
   */
  private class NowTriggerInstance implements Trigger.Instance {
    private final TriggerDefinition triggerDefinition;

    /**
     * Creates instance of the trigger instance
     * @param triggerDefinition trigger definition
     */
    public NowTriggerInstance(TriggerDefinition triggerDefinition) {
      this.triggerDefinition = triggerDefinition;
    }

    @Override
    public void activate(Trigger.Context context) throws DataProcessorException, InvalidDefinitionException {
      // submit task definition now.
      context.submit(triggerDefinition.getTaskDefinition());
    }

    
    @Override
    public String toString() {
      return String.format("IMMEDIATE TRIGGER FOR : %s", triggerDefinition);
    }
    
    @Override
    public void close() throws Exception {
      // nothing to close (yet)
    }
  }
}
