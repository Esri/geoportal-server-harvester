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
import com.esri.geoportal.harvester.api.TriggerInstance;
import com.esri.geoportal.harvester.api.defs.TriggerInstanceDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 'Now' trigger. Triggers harvesting immediately. . The syntax of the request: 
   <pre><code>
   {
     "type": "NOW",
     taskDefinition: &lt;task definition&gt;
   }
   </code></pre>
 */
public class NowTrigger implements Trigger {
  private static final Logger LOG = LoggerFactory.getLogger(NowTrigger.class);
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
  public TriggerInstance createInstance(TriggerInstanceDefinition triggerDefinition) throws InvalidDefinitionException {
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
  private class NowTriggerInstance implements TriggerInstance {
    final TriggerInstanceDefinition triggerDefinition;

    /**
     * Creates instance of the trigger instance
     * @param triggerDefinition trigger definition
     */
    public NowTriggerInstance(TriggerInstanceDefinition triggerDefinition) {
      this.triggerDefinition = triggerDefinition;
    }

    @Override
    public TriggerInstanceDefinition getTriggerDefinition() {
      return triggerDefinition;
    }

    @Override
    public void activate(TriggerInstance.Context context) throws DataProcessorException, InvalidDefinitionException {
      LOG.info(String.format("Task is being submitted now: %s", triggerDefinition.getTaskDefinition()));
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
