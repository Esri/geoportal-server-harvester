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

import com.esri.geoportal.harvester.engine.utils.Statistics;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.engine.utils.ReportBuilder;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Report statistics.
 */
public class ReportStatistics extends ProgressLogger implements ReportBuilder, Statistics {
  private final Logger LOG = LoggerFactory.getLogger(ReportStatistics.class);
  
  private Date startDate;
  private Date endDate;
  
  private long acquired;
  private long succeeded;
  private long harvestFailed;
  private long publishFailed;
  
  private boolean failure;
  
  @Override
  public Date getStartDate() {
    return startDate;
  }

  @Override
  public Date getEndDate() {
    return endDate;
  }

  @Override
  public long getSucceeded() {
    return succeeded;
  }

  @Override
  public long getHarvestFailed() {
    return harvestFailed;
  }

  @Override
  public long getPublishFailed() {
    return publishFailed;
  }

  @Override
  public boolean isFailure() {
    return failure;
  }

  @Override
  public void started(ProcessInstance process) {
    startDate = Calendar.getInstance().getTime();
    LOG.info(String.format("Harvesting of %s started at %s", process, startDate));
  }

  @Override
  public void acquire(ProcessInstance process, DataReference dataReference) {
    acquired++;
    super.acquire(process, dataReference);
  }

  @Override
  public void completed(ProcessInstance process) {
    endDate = Calendar.getInstance().getTime();
    LOG.info(String.format("Harvesting of %s completed at %s. No. succeded: %d, no. failed: %d", process, endDate, succeeded, harvestFailed+publishFailed));
  }

  @Override
  public long getAcquired() {
    return acquired;
  }

  @Override
  public void success(ProcessInstance process, DataReference dataReference) {
    ++succeeded;
    super.success(process, dataReference);
  }

  @Override
  public void error(ProcessInstance process, DataInputException ex) {
    ++harvestFailed;
    super.error(process, ex);
  }

  @Override
  public void error(ProcessInstance process, DataOutputException ex) {
    ++publishFailed;
  }

  @Override
  public void error(ProcessInstance process, com.esri.geoportal.harvester.api.ex.DataProcessorException ex) {
    failure = true;
  }
  
  @Override
  public String toString() {
    return String.format("STATISTICS :: start: %s, end: %s, succeeded: %d, harvested failed: %d, published failed: %d, failure: %b", startDate, endDate, succeeded, harvestFailed, publishFailed, failure);
  }
}
