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
package com.esri.geoportal.harvester.engine.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * History.
 */
public class History extends ArrayList<History.Event> {
  private static final Logger LOG = LoggerFactory.getLogger(History.class);
  
  /**
   * Gets last event.
   * @return last event
   */
  public History.Event getLastEvent() {
    return stream()
              .sorted((left,right)->0-left.getStartTimestamp().compareTo(right.getStartTimestamp()))
              .findFirst()
              .orElse(null);
  }
  
  /**
   * History event.
   */
  public static final class Event {
    private UUID uuid;
    private UUID taskId;
    private Date startTimestamp;
    private Date endTimestamp;
    private Report report;

    /**
     * Gest event id.
     * @return event id
     */
    public UUID getUuid() {
      return uuid;
    }

    /**
     * Sets event id.
     * @param uuid event id
     */
    public void setUuid(UUID uuid) {
      this.uuid = uuid;
    }

    /**
     * Gets task id.
     * @return task id
     */
    public UUID getTaskId() {
      return taskId;
    }

    /**
     * Sets task id.
     * @param taskId task id 
     */
    public void setTaskId(UUID taskId) {
      this.taskId = taskId;
    }

    /**
     * Gets start timestamp.
     * @return start timestamp
     */
    public Date getStartTimestamp() {
      return startTimestamp;
    }

    /**
     * Sets start timestamp.
     * @param startTimestamp start timestamp 
     */
    public void setStartTimestamp(Date startTimestamp) {
      this.startTimestamp = startTimestamp;
    }

    /**
     * Gets end timestamp.
     * @return end timestamp
     */
    public Date getEndTimestamp() {
      return endTimestamp;
    }

    /**
     * Sets end timestamp.
     * @param endTimestamp end timestamp 
     */
    public void setEndTimestamp(Date endTimestamp) {
      this.endTimestamp = endTimestamp;
    }

    /**
     * Gets report.
     * @return report
     */
    public Report getReport() {
      return report;
    }

    /**
     * Sets report.
     * @param report report
     */
    public void setReport(Report report) {
      this.report = report;
    }
  }
  
  /**
   * History report.
   */
  public static final class Report {
    /** number of records acquired. */
    public long acquired;
    /** number of records created. */
    public long created;
    /** number of records updated. */
    public long updated;
    /** number of records failed. */
    public long failed;
    public Long failedToHarvest;
    public Long failedToPublish;
  }
}
