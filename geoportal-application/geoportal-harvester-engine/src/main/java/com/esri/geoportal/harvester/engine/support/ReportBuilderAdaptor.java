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
package com.esri.geoportal.harvester.engine.support;

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.engine.managers.ReportBuilder;
import com.esri.geoportal.harvester.api.ex.DataException;

/**
 * Report builder adaptor.
 */
public class ReportBuilderAdaptor implements Processor.Listener {
  private final Processor.Process process;
  private final ReportBuilder reportBuilder;

  /**
   * Creates instance of the adaptor.
   * @param process process
   * @param reportBuilder report builder
   */
  public ReportBuilderAdaptor(Processor.Process process, ReportBuilder reportBuilder) {
    this.process = process;
    this.reportBuilder = reportBuilder;
  }

  @Override
  public void onStatusChange(Processor.Status newStatus) {
    switch (newStatus) {
      case working:
        reportBuilder.started(process);
        break;
      case completed:
        reportBuilder.completed(process);
        break;
    }
  }

  @Override
  public void onDataProcessed(DataReference dataReference) {
    reportBuilder.success(process, dataReference);
  }

  @Override
  public void onError(DataException ex) {
    if (ex instanceof DataInputException) {
      reportBuilder.error(process, (DataInputException)ex);
    }
    if (ex instanceof DataOutputException) {
      reportBuilder.error(process, (DataOutputException)ex);
    }
  }
  
}
