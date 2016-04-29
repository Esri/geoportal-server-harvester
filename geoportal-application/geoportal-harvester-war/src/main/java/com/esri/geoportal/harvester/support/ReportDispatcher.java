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

import com.esri.geoportal.harvester.api.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.DataInputException;
import com.esri.geoportal.harvester.engine.Process;
import com.esri.geoportal.harvester.engine.ReportBuilder;
import java.util.Arrays;
import java.util.List;

/**
 * Report dispatcher.
 */
public class ReportDispatcher implements ReportBuilder {
  private final List<ReportBuilder> builders;

  /**
   * Creates instance of report dispatcher.
   * @param builders array of builders
   */
  public ReportDispatcher(ReportBuilder...builders) {
    this.builders = Arrays.asList(builders);
  }

  @Override
  public void started(Process process) {
    builders.forEach(b->b.started(process));
  }

  @Override
  public void completed(Process process) {
    builders.forEach(b->b.completed(process));
  }

  @Override
  public void success(Process process, DataReference dataReference) {
    builders.forEach(b->b.success(process, dataReference));
  }

  @Override
  public void error(Process process, DataInputException ex) {
    builders.forEach(b->b.error(process, ex));
  }

  @Override
  public void error(Process process, DataOutputException ex) {
    builders.forEach(b->b.error(process, ex));
  }

  @Override
  public void error(Process process, com.esri.geoportal.harvester.api.DataProcessorException ex) {
    builders.forEach(b->b.error(process, ex));
  }
}
