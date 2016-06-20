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
package com.esri.geoportal.harvester.api;

import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.defs.TriggerInstanceDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.util.Date;

/**
 * Trigger.
 */
public interface Trigger extends AutoCloseable {
  /**
   * Gets type of the trigger.
   * @return type of the trigger
   */
  String getType();
  
  /**
   * Gets UI template.
   * @return template
   */
  UITemplate getTemplate();
  
  /**
   * Creates instance of the trigger.
   * @param triggerDefinition trigger instance definition
   * @return instance of the trigger
   * @throws InvalidDefinitionException if trigger definition is invalid
   */
  TriggerInstance createInstance(TriggerInstanceDefinition triggerDefinition) throws InvalidDefinitionException;
}
