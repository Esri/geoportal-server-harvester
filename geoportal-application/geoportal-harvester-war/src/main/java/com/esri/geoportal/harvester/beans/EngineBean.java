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

import com.esri.geoportal.harvester.engine.services.BrokersService;
import com.esri.geoportal.harvester.engine.services.ExecutionService;
import com.esri.geoportal.harvester.engine.services.ProcessesService;
import com.esri.geoportal.harvester.engine.services.TasksService;
import com.esri.geoportal.harvester.engine.services.TemplatesService;
import com.esri.geoportal.harvester.engine.services.TriggersService;
import com.esri.geoportal.harvester.engine.defaults.DefaultEngine;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DefaultEngine bean.
 */
@Service
public class EngineBean extends DefaultEngine {
  private static final Logger LOG = LoggerFactory.getLogger(EngineBean.class);

  /**
   * Creates instance of the engine bean.
   * @param templatesService templates service
   * @param brokersService brokers service
   * @param tasksService tasks service
   * @param processesService processes service
   * @param triggersService triggers service
   * @param executionService execution service
   */
  @Autowired
  public EngineBean(
          TemplatesService templatesService,
          BrokersService brokersService,
          TasksService tasksService,
          ProcessesService processesService,
          TriggersService triggersService,
          ExecutionService executionService
  ) {
    super(
            templatesService,
            brokersService,
            tasksService,
            processesService,
            triggersService,
            executionService
    );
  }
  
  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    getTriggersService().activateTriggerInstances();
    LOG.info("EngineBean initialized.");
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    getTriggersService().deactivateTriggerInstances();
    LOG.info(String.format("EngineBean destroyed."));
  }
}
