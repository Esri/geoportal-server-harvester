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
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.specs.OutputBroker.PublishingStatus;
import static com.esri.geoportal.harvester.api.specs.OutputBroker.PublishingStatus.created;
import com.esri.geoportal.harvester.engine.managers.History;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * History manager adaptor.
 */
public class HistoryManagerAdaptor implements ProcessInstance.Listener {
  private static final Logger LOG = LoggerFactory.getLogger(HistoryManagerAdaptor.class);
  
  private final UUID uuid;
  private final ProcessInstance processInstance;
  private final HistoryManager historyManager;
  private final History.Event event = new History.Event();
  private final History.Report report = new History.Report();
  private Date startDate;
  private Date endDate;

  /**
   * Creates instance of the adaptor.
   * @param uuid process id
   * @param processInstance process instance
   * @param historyManager history manager.
   */
  public HistoryManagerAdaptor(UUID uuid, ProcessInstance processInstance, HistoryManager historyManager) {
    this.uuid = uuid;
    this.processInstance = processInstance;
    this.historyManager = historyManager;
  }
  

  @Override
  public void onStatusChange(ProcessInstance.Status status) {
    switch (status) {
      case submitted:
        event.setUuid(UUID.randomUUID());
        event.setTaskId(uuid);
        break;
      case working:
        if (startDate==null) {
          startDate = new Date();
          event.setStartTimestamp(startDate);
        }
        break;
      case completed:
        if (endDate==null) {
          endDate = new Date();
          event.setEndTimestamp(endDate);
        }
        event.setReport(report);
        {
          try {
            historyManager.create(event);
          } catch (CrudsException ex) {
            LOG.error(String.format("Error creating history event for: %s", uuid), ex);
          }
        }
        break;
    }
  }

  @Override
  public void onDataAcquired(DataReference dataReference) {
    report.acquired++;
  }

  @Override
  public void onDataProcessed(DataReference dataReference, PublishingStatus status) {
    switch (status) {
      case created:
        report.created++;
        break;
      case updated:
        report.updated++;
        break;
    }
  }

  @Override
  public void onError(DataException ex) {
    report.failed++;
  }
  
}
