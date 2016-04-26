/*
 * Copyright 2016 Esri, Inc..
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

import com.esri.geoportal.harvester.engine.ReportBuilder;
import com.esri.geoportal.harvester.engine.Engine;
import com.esri.geoportal.harvester.engine.ProcessManager;
import com.esri.geoportal.harvester.engine.TaskManager;
import com.esri.geoportal.harvester.engine.support.DataDestinationRegistry;
import com.esri.geoportal.harvester.engine.support.DataSourceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Engine bean.
 */
@Service
public class EngineBean extends Engine {

  @Autowired
  public EngineBean(ReportBuilder reportBuilder, TaskManager taskManager, ProcessManager processManager, DataSourceRegistry dsReg, DataDestinationRegistry dpReg) {
    super(reportBuilder, taskManager, processManager, dsReg, dpReg);
  }
}
