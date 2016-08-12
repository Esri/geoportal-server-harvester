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

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.engine.support.ReportBuilder;
import java.util.UUID;

/**
 * Report manager.
 */
public interface ReportManager {
  
  /**
   * Creates report builder.
   * @param uuid process id
   * @param processInstance process instance
   * @return report builder
   */
  ReportBuilder createReportBuilder(UUID uuid, ProcessInstance processInstance);
}
