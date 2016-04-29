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

import com.esri.geoportal.harvester.api.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.DataInputException;
import com.esri.geoportal.harvester.engine.Process;
import com.esri.geoportal.harvester.engine.ReportBuilder;
import com.esri.geoportal.harvester.support.ReportDispatcher;
import com.esri.geoportal.harvester.support.ReportLogger;
import com.esri.geoportal.harvester.support.ReportStatistics;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/**
 * Report builder bean.
 */
@Service
public class ReportBuilderBean implements ReportBuilder {
  private ReportBuilder rb;
  
  @PostConstruct
  public void init () {
    ReportStatistics rs = new ReportStatistics();
    ReportLogger rl = new ReportLogger();
    
    rb = new ReportDispatcher(rs, rl);
  }

  @Override
  public void started(Process process) {
    rb.started(process);
  }

  @Override
  public void completed(Process process) {
    rb.completed(process);
  }

  @Override
  public void success(Process process, DataReference dataReference) {
    rb.success(process, dataReference);
  }

  @Override
  public void error(Process process, DataInputException ex) {
    rb.error(process, ex);
  }

  @Override
  public void error(Process process, DataOutputException ex) {
    rb.error(process, ex);
  }

  @Override
  public void error(Process process, com.esri.geoportal.harvester.api.DataProcessorException ex) {
    rb.error(process, ex);
  }
}
