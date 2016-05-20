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
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.util.Map;

/**
 * Immediate trigger.
 */
public class ImmediateTrigger implements Trigger {
  public static final String TYPE = "IMMEDIATE";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate() {
    return new UITemplate(getType(), "Harvest now", null);
  }

  @Override
  public Instance createInstance(EntityDefinition triggerDefinition) throws InvalidDefinitionException {
    if (!getType().equals(triggerDefinition.getType())) {
      throw new InvalidDefinitionException(String.format("Invalid trigger definition: %s", triggerDefinition));
    }
    return new ImmediateTriggerInstance(triggerDefinition.getProperties());
  }
  
  /**
   * Immediate trigger instance.
   */
  private class ImmediateTriggerInstance implements Trigger.Instance {
    private final Map<String,String> arguments;

    public ImmediateTriggerInstance(Map<String, String> arguments) {
      this.arguments = arguments;
    }
    
    @Override
    public Trigger getTrigger() {
      return ImmediateTrigger.this;
    }
    

    @Override
    public void activate(Trigger.Context context, TaskDefinition taskDefinition) throws DataProcessorException, InvalidDefinitionException {
      context.submit(taskDefinition);
    }

    @Override
    public void close() throws Exception {
      // nothing to close (yet)
    }
  }
}
