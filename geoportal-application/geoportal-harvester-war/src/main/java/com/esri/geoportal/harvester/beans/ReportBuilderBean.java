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

import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.engine.managers.ReportBuilder;
import com.esri.geoportal.harvester.support.ReportDispatcher;
import com.esri.geoportal.harvester.support.ReportLogger;
import com.esri.geoportal.harvester.support.ReportStatistics;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Report builder bean.
 */
@Service
public class ReportBuilderBean implements ReportBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(ReportBuilderBean.class);
  
  private ReportBuilder rb;
  
  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init () {
    ReportStatistics rs = new ReportStatistics();
    ReportLogger rl = new ReportLogger();
    
    rb = new ReportDispatcher(rs, rl);
    
    LOG.info("ReportBuilderBean initialized.");
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    LOG.info(String.format("ReportBuilderBean destroyed."));
  }

  @Override
  public void started(ProcessInstance process) {
    rb.started(process);
  }

  @Override
  public void completed(ProcessInstance process) {
    rb.completed(process);
  }

  @Override
  public void success(ProcessInstance process, DataReference dataReference) {
    rb.success(process, dataReference);
  }

  @Override
  public void error(ProcessInstance process, DataInputException ex) {
    rb.error(process, ex);
  }

  @Override
  public void error(ProcessInstance process, DataOutputException ex) {
    rb.error(process, ex);
  }

  @Override
  public void error(ProcessInstance process, com.esri.geoportal.harvester.api.ex.DataProcessorException ex) {
    rb.error(process, ex);
  }
}
