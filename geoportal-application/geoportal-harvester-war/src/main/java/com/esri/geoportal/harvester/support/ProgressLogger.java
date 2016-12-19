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

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.engine.utils.ReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Progress logger.
 */
public abstract class ProgressLogger implements ReportBuilder {
  private final Logger LOG = LoggerFactory.getLogger(ProgressLogger.class);
  private final long STATUS_LOG_MODULO = 250;
  private int counter;

  @Override
  public void acquire(ProcessInstance process, DataReference dataReference) {
    counter++;
  }

  @Override
  public void success(ProcessInstance process, DataReference dataReference) {
    printStatusLog(process);
  }

  @Override
  public void error(ProcessInstance process, DataInputException ex) {
    printStatusLog(process);
  }

  private void printStatusLog(ProcessInstance process) {
    if (counter % STATUS_LOG_MODULO == 0) {
      LOG.info(String.format("Harvesting of %s progress: %d", process, counter));
    }
  }
}
