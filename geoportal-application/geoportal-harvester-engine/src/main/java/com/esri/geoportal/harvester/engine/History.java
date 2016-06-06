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
package com.esri.geoportal.harvester.engine;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * History.
 */
public class History extends ArrayList<History.Event> implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(History.class);
  
  /**
   * Gets last event.
   * @return last event or <code>null</code> if no events
   */
  public Event lastEvent() {
    return stream()
            .sorted((left,right)->0-left.getTimestamp().compareTo(right.getTimestamp()))
            .findFirst()
            .orElse(null);
  }

  @Override
  public void close() throws IOException {
    stream().forEach(e->{
      try {
        e.close();
      } catch (IOException ex) {
        LOG.warn(String.format("Error closing event."), ex);
      }
    });
  }
  
  /**
   * History event.
   */
  public static class Event implements Closeable {
    private UUID uuid;
    private UUID taskId;
    private Date timestamp;

    public UUID getUuid() {
      return uuid;
    }

    public void setUuid(UUID uuid) {
      this.uuid = uuid;
    }

    public UUID getTaskId() {
      return taskId;
    }

    public void setTaskId(UUID taskId) {
      this.taskId = taskId;
    }

    public Date getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
    }

    @Override
    public void close() throws IOException {
      // TODO implement CLOB close
    }
  }
}
