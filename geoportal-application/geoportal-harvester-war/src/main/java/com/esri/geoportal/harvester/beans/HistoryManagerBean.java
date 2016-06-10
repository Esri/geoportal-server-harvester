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
package com.esri.geoportal.harvester.beans;

import com.esri.geoportal.harvester.engine.History;
import com.esri.geoportal.harvester.engine.HistoryManager;
import com.esri.geoportal.harvester.engine.support.CrudsException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * History manager bean.
 */
@Service
public class HistoryManagerBean implements HistoryManager {
  private static final Logger LOG = LoggerFactory.getLogger(HistoryManagerBean.class);

  @Autowired
  private DataSource dataSource;

  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS EVENTS ( id varchar(38) PRIMARY KEY, taskid varchar(38) NOT NULL, completed TIMESTAMP NOT NULL, report CLOB ) ;"
                  + "CREATE INDEX IF NOT EXISTS EVENTS_TASKID_IDX ON EVENTS(taskid);");
        ) {
      st.execute();
      LOG.info("HistoryManagerBean initialized.");
    } catch (SQLException ex) {
      LOG.info("Error initializing history database", ex);
    }
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    LOG.info(String.format("HistoryManagerBean destroyed."));
  }

  @Override
  public UUID create(History.Event data) throws CrudsException {
    UUID id = UUID.randomUUID();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("INSERT INTO EVENTS (taskid,completed,id) VALUES (?,?,?)");
        ) {
      st.setString(1, data.getTaskId().toString());
      st.setTimestamp(2, new Timestamp(data.getTimestamp().getTime()));
      st.setString(3, data.getUuid().toString());
      st.executeUpdate();
    } catch (SQLException ex) {
      throw new CrudsException("Error creating history event", ex);
    }
    return id;
  }

  @Override
  public boolean delete(UUID id) throws CrudsException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("DELETE FROM EVENTS WHERE ID = ?");
        ) {
      st.setString(1, id.toString());
      return st.executeUpdate()>0;
    } catch (SQLException ex) {
      throw new CrudsException("Error deleting history event definition", ex);
    }
  }

  @Override
  public History.Event read(UUID id) throws CrudsException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT taskid,completed,id FROM EVENTS WHERE ID = ?");
        ) {
      st.setString(1, id.toString());
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        History.Event event = new History.Event();
        event.setTaskId(UUID.fromString(rs.getString(1)));
        event.setTimestamp(new Date(rs.getTimestamp(2).getTime()));
        event.setUuid(UUID.fromString(rs.getString(3)));
        return event;
      }
    } catch (SQLException ex) {
      throw new CrudsException("Error reading history event", ex);
    }
    
    return null;
  }

  @Override
  public Collection<Map.Entry<UUID, History.Event>> select() throws CrudsException {
    HashMap<UUID, History.Event> map = new HashMap<>();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT taskid,completed,id FROM EVENTS");
        ) {
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        History.Event event = new History.Event();
        event.setTaskId(UUID.fromString(rs.getString(1)));
        event.setTimestamp(new Date(rs.getTimestamp(2).getTime()));
        event.setUuid(UUID.fromString(rs.getString(3)));
        map.put(event.getUuid(), event);
      }
    } catch (SQLException ex) {
      throw new CrudsException("Error selecting broker definition", ex);
    }
    return map.entrySet();
  }

  @Override
  public boolean update(UUID id, History.Event data) throws CrudsException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("UPDATE EVENTS SET (taskid = ?, completed = ?) WHERE ID = ?");
        ) {
      st.setString(1, data.getTaskId().toString());
      st.setTimestamp(2, new Timestamp(data.getTimestamp().getTime()));
      st.setString(3, id.toString());
      return st.executeUpdate()>0;
    } catch (SQLException ex) {
      throw new CrudsException("Error updating history event", ex);
    }
  }

  @Override
  public History buildHistory(UUID taskid) throws CrudsException {
    History history = new History();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT taskid,completed,id FROM EVENTS WHERE taskid = ?");
        ) {
      st.setString(1, taskid.toString());
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        History.Event event = new History.Event();
        event.setTaskId(UUID.fromString(rs.getString(1)));
        event.setTimestamp(new Date(rs.getTimestamp(2).getTime()));
        event.setUuid(UUID.fromString(rs.getString(3)));
        history.add(event);
      }
      return history;
    } catch (SQLException ex) {
      throw new CrudsException("Error selecting broker definition", ex);
    }
  }
  
}
