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
package com.esri.geoportal.cli.boot;

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.engine.managers.ReportManager;
import com.esri.geoportal.harvester.engine.utils.ReportBuilder;
import java.util.UUID;

/**
 * In-memory report manager.
 */
public class MemReportManager implements ReportManager {

  @Override
  public ReportBuilder createReportBuilder(UUID uuid, ProcessInstance processInstance) {
    ReportBuilder rb = new ReportBuilder() {
      private long acquired;
      private long published;
      private long failed;
      
      @Override
      public void started(ProcessInstance process) {
        System.out.println(String.format("Started harvesting of %s", processInstance.getTask().getTaskDefinition()));
      }

      @Override
      public void completed(ProcessInstance process) {
        System.out.println(String.format("Completed harvesting of %s", processInstance.getTask().getTaskDefinition()));
        System.out.println(String.format("Acquired: %d, published: %d, failed: %d", acquired, published, failed));
      }

      @Override
      public void acquire(ProcessInstance process, DataReference dataReference) {
        acquired++;
        System.out.println(String.format("Acquired [%d]: %s", acquired, dataReference.getSourceUri()));
      }

      @Override
      public void success(ProcessInstance process, DataReference dataReference) {
        published++;
        System.out.println(String.format("Published[%d]: %s ", published, dataReference.getSourceUri()));
      }

      @Override
      public void error(ProcessInstance process, DataInputException ex) {
        failed++;
        ex.printStackTrace(System.err);
      }

      @Override
      public void error(ProcessInstance process, DataOutputException ex) {
        failed++;
        ex.printStackTrace(System.err);
      }

      @Override
      public void error(ProcessInstance process, DataProcessorException ex) {
        failed++;
        ex.printStackTrace(System.err);
      }
    };
    return rb;
  }
  
}
