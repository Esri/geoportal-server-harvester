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
package com.esri.geoportal.harvester.support;

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.engine.utils.ReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Error logger.
 */
public abstract class ErrorLogger implements ReportBuilder {
  private final Logger LOG = LoggerFactory.getLogger(ErrorLogger.class);

  @Override
  public void error(ProcessInstance process, DataInputException ex) {
    logError(process, ex);
  }

  @Override
  public void error(ProcessInstance process, DataOutputException ex) {
    logError(process, ex);
  }

  @Override
  public void error(ProcessInstance process, com.esri.geoportal.harvester.api.ex.DataProcessorException ex) {
    logError(process, ex);
  }
  
  private void logError(ProcessInstance process, Exception ex) {
    if (LOG.isDebugEnabled()) {
      LOG.error(String.format("Error processing task: %s | %s", process, ex.getMessage()), ex);
    } else {
      LOG.error(String.format("Error processing task: %s | %s", process, ex.getMessage()));
    }
  }
}
