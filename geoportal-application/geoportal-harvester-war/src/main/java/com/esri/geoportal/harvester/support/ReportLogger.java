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
package com.esri.geoportal.harvester.support;

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Report logger.
 */
public class ReportLogger extends ErrorLogger {
  private final Logger LOG = LoggerFactory.getLogger(ReportLogger.class);

  @Override
  public void started(ProcessInstance process) {
    LOG.info(String.format("Started processing task: %s", process));
  }

  @Override
  public void completed(ProcessInstance process) {
    LOG.info(String.format("Completed processing task: %s", process));
  }

  @Override
  public void acquire(ProcessInstance process, DataReference dataReference) {
    LOG.debug(String.format("Success acquiring data: %s during processing task: %s", dataReference, process));
  }

  @Override
  public void success(ProcessInstance process, DataReference dataReference) {
    LOG.debug(String.format("Success harvesting data: %s during processing task: %s", dataReference, process));
  }
}
