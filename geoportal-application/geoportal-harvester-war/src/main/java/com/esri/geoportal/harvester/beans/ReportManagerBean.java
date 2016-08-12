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

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.engine.managers.ReportManager;
import com.esri.geoportal.harvester.engine.registers.StatisticsRegistry;
import com.esri.geoportal.harvester.engine.support.ReportBuilder;
import com.esri.geoportal.harvester.support.ReportDispatcher;
import com.esri.geoportal.harvester.support.ReportLogger;
import com.esri.geoportal.harvester.support.ReportStatistics;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Report manager bean.
 */
@Service
public class ReportManagerBean implements ReportManager {
  private static final Logger LOG = LoggerFactory.getLogger(ReportManagerBean.class);
  
  @Autowired
  private StatisticsRegistry statisticsRegistry;
  
  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    LOG.info(String.format("ReportManagerBean created."));
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    LOG.info(String.format("ReportManagerBean destroyed."));
  }

  @Override
  public ReportBuilder createReportBuilder(UUID uuid, ProcessInstance processInstance) {
    ReportLogger reportLogger = new ReportLogger();
    ReportStatistics reportStatistics = new ReportStatistics() {
      @Override
      public void completed(ProcessInstance process) {
        super.completed(process);
        statisticsRegistry.remove(uuid);
      }
    };
    statisticsRegistry.put(uuid, reportStatistics);
    ReportDispatcher reportDispatcher = new ReportDispatcher(reportLogger, reportStatistics);
    return reportDispatcher;
  }
  
}
