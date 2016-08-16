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
package com.esri.geoportal.harvester.beans;

import com.esri.geoportal.harvester.engine.services.ExecutionService;
import com.esri.geoportal.harvester.engine.defaults.DefaultTriggersService;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.registers.TriggerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Triggers service bean.
 */
@Service
public class TriggersServiceBean extends DefaultTriggersService {

  /**
   * Creates instance of the bean.
   * @param triggerRegistry trigger registry
   * @param triggerManager trigger manager
   * @param triggerInstanceManager trigger instance manager
   * @param executionService execution service
   */
  @Autowired
  public TriggersServiceBean(TriggerRegistry triggerRegistry, TriggerManager triggerManager, TriggerInstanceManager triggerInstanceManager, ExecutionService executionService) {
    super(triggerRegistry, triggerManager, triggerInstanceManager,executionService);
  }
  
}
