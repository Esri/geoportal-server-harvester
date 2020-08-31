/*
 * Copyright 2016 Piotr Andzel.
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

import com.esri.geoportal.harvester.engine.managers.History;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Event response.
 */
public final class EventResponse {
  private final UUID uuid;
  private final Date startTimestamp;
  private final Date endTimestamp;
  private final long acquired;
  private final long created;
  private final long updated;
  private final long failed;
  private final Long failedToHarvest;
  private final Long failedToPublish;
  private final List<String> details;

  /**
   * Creates instance of the response.
   * @param uuid event id
   * @param startTimestamp event start timestamp
   * @param endtTimestamp event end timestamp
   * @param acquired number of acquired records
   * @param created number of newly created records
   * @param updated number of updated records
   * @param failed number of failed records
   * @param failedToHarvest number of records failed to harvest
   * @param failedToPublish number of records failed to publish
   */
  public EventResponse(
          UUID uuid, 
          Date startTimestamp, 
          Date endtTimestamp, 
          long acquired, long created, long updated, long failed,
          Long failedToHarvest, Long failedToPublish,
          List<String> details) {
    this.uuid = uuid;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endtTimestamp;
    this.acquired = acquired;
    this.created = created;
    this.updated = updated;
    this.failed = failed;
    this.failedToHarvest = failedToHarvest;
    this.failedToPublish = failedToPublish;
    this.details = details;
  }
  
  /**
   * Creates instance of the response.
   * @param event history event
   */
  public EventResponse(History.Event event) {
    this(
            event.getUuid(), 
            event.getStartTimestamp(), 
            event.getEndTimestamp(), 
            event.getReport()!=null? event.getReport().acquired: 0, 
            event.getReport()!=null? event.getReport().created: 0, 
            event.getReport()!=null? event.getReport().updated: 0, 
            event.getReport()!=null? event.getReport().failed: 0,
            event.getReport()!=null? event.getReport().failedToHarvest: null,
            event.getReport()!=null? event.getReport().failedToPublish: null,
            event.getReport()!=null? event.getReport().details.keySet().stream().collect(Collectors.toList()): null);
  }

  /**
   * Gets event id.
   * @return event id
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   * Gets event start timestamp.
   * @return event start timestamp
   */
  public Date getStartTimestamp() {
    return startTimestamp;
  }

  /**
   * Gets event end timestamp.
   * @return event end timestamp
   */
  public Date getEndTimestamp() {
    return endTimestamp;
  }

  /**
   * Gets number of acquired records.
   * @return number of acquired records
   */
  public long getAcquired() {
    return acquired;
  }

  /**
   * Gets number of newly created records.
   * @return number of newly created records
   */
  public long getCreated() {
    return created;
  }

  /**
   * Gets number of updated records.
   * @return number of updated records
   */
  public long getUpdated() {
    return updated;
  }

  /**
   * Gets number of failed records.
   * @return number of failed records
   */
  public long getFailed() {
    return failed;
  }

  /**
   * Gets number of records failed to harvest.
   * @return number of records failed to harvest or <code>null</code> if information not available
   */
  public Long getFailedToHarvest() {
    return failedToHarvest;
  }

  /**
   * Gets number of records failed to publish.
   * @return number of records failed to publish or <code>null</code> if information not available
   */
  public Long getFailedToPublish() {
    return failedToPublish;
  }

  /**
   * Gest details.
   * @return details
   */
  public List<String> getDetails() {
    return details;
  }
  
  
}
