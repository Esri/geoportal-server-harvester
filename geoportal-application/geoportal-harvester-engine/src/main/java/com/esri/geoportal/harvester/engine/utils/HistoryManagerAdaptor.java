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
package com.esri.geoportal.harvester.engine.utils;

import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import com.esri.geoportal.commons.utils.ExceptionUtils;
import com.esri.geoportal.harvester.api.base.BaseProcessInstanceListener;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.engine.managers.History;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * History manager adaptor.
 */
public class HistoryManagerAdaptor extends BaseProcessInstanceListener {

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
   *
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
        report.failedToHarvest = 0L;
        report.failedToPublish = 0L;
        break;
      case working:
        if (startDate == null) {
          startDate = new Date();
          event.setStartTimestamp(startDate);
        }
        break;
      case completed: {
        if (startDate == null) {
          startDate = new Date();
          event.setStartTimestamp(startDate);
        }
        if (endDate == null) {
          endDate = new Date();
          event.setEndTimestamp(endDate);
        }
        event.setReport(report);
        try {
          historyManager.create(event);
        } catch (CrudlException ex) {
          LOG.error(formatForLog("Error creating history event for: %s", uuid), ex);
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
    report.created += status.getCreated();
    report.updated += status.getUpdated();
  }

  @Override
  public void onError(DataException ex) {
    report.failed++;
    
    Throwable dataOutputException = ExceptionUtils.unfoldCauses(ex).stream().filter((Throwable t) -> t instanceof DataOutputException).findAny().orElse(null);
    if (dataOutputException != null) {
      report.failedToPublish ++;
      DataOutputException outex = (DataOutputException) dataOutputException;
      try {
        historyManager.storeFailedDataId(event.getUuid(), outex.getFetchableDataId());
      } catch (CrudlException ex2) {
        LOG.error(formatForLog("Error storing failed data id: %s %s [%s]", uuid, event.getUuid(), outex.getDataId()), ex);
      }
    } else {
      report.failedToHarvest ++;
    }
  }

}
