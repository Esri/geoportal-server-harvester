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
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.engine.ReportBuilder;
import com.esri.geoportal.harvester.support.ReportDispatcher;
import com.esri.geoportal.harvester.support.ReportLogger;
import com.esri.geoportal.harvester.support.ReportStatistics;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.esri.geoportal.harvester.api.ProcessHandle;

/**
 * Report builder bean.
 */
@Service
public class ReportBuilderBean implements ReportBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(ReportBuilderBean.class);
  
  private ReportBuilder rb;
  
  @PostConstruct
  public void init () {
    ReportStatistics rs = new ReportStatistics();
    ReportLogger rl = new ReportLogger();
    
    rb = new ReportDispatcher(rs, rl);
    
    LOG.info("ReportBuilderBean initialized.");
  }

  @Override
  public void started(ProcessHandle process) {
    rb.started(process);
  }

  @Override
  public void completed(ProcessHandle process) {
    rb.completed(process);
  }

  @Override
  public void success(ProcessHandle process, DataReference dataReference) {
    rb.success(process, dataReference);
  }

  @Override
  public void error(ProcessHandle process, DataInputException ex) {
    rb.error(process, ex);
  }

  @Override
  public void error(ProcessHandle process, DataOutputException ex) {
    rb.error(process, ex);
  }

  @Override
  public void error(ProcessHandle process, com.esri.geoportal.harvester.api.ex.DataProcessorException ex) {
    rb.error(process, ex);
  }
}
