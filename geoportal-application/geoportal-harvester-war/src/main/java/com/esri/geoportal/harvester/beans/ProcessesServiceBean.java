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
import com.esri.geoportal.harvester.engine.defaults.DefaultProcessesService;
import com.esri.geoportal.harvester.engine.managers.ProcessManager;
import com.esri.geoportal.harvester.engine.managers.ReportManager;
import com.esri.geoportal.harvester.engine.registers.StatisticsRegistry;
import com.esri.geoportal.harvester.engine.utils.CrudlException;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Processes service bean.
 */
@Service
public class ProcessesServiceBean extends DefaultProcessesService {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessesServiceBean.class);

  /**
   * Creates instance of the bean.
   * @param processManager process manager
   * @param reportManager report manager
   * @param statisticsRegistry statistics registry
   */
  @Autowired
  public ProcessesServiceBean(ProcessManager processManager, ReportManager reportManager, StatisticsRegistry statisticsRegistry) {
    super(processManager, reportManager, statisticsRegistry);
  }
  
  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    LOG.info("ProcessesServiceBean initialized.");
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    try {
      processManager.list().stream().map(Map.Entry::getValue).forEach(ProcessInstance::abort);
    } catch (CrudlException ex) {
    }
    LOG.info(String.format("ProcessesServiceBean destroyed."));
  }
  
}
