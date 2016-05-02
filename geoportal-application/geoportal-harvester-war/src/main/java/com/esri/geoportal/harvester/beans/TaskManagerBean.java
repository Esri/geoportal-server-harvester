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
package com.esri.geoportal.harvester.beans;

import static com.esri.geoportal.harvester.support.TaskDefinitionSerializer.deserializeTaskDef;
import static com.esri.geoportal.harvester.support.TaskDefinitionSerializer.serializeTaskDef;
import com.esri.geoportal.harvester.engine.TaskDefinition;
import com.esri.geoportal.harvester.engine.TaskManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task manager bean.
 */
@Service
public class TaskManagerBean implements TaskManager {

  private final Logger LOG = LoggerFactory.getLogger(TaskManager.class);

  @Autowired
  private DataSource dataSource;

  /**
   * Initializes database.
   */
  @PostConstruct
  public void init() {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("CREATE TABLE IF NOT EXISTS TASKS ( id varchar(38) PRIMARY KEY, taskDefinition varchar(1024) NOT NULL)");
        ) {
      st.execute();
    } catch (SQLException ex) {
      LOG.info("Error initializing task database", ex);
    }
  }

  @Override
  public UUID create(TaskDefinition taskDef) {
    UUID id = UUID.randomUUID();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("INSERT INTO TASKS (taskDefinition,id) VALUES (?,?)");
        ) {
      st.setString(1, serializeTaskDef(taskDef));
      st.setString(2, id.toString());
      st.executeUpdate();
    } catch (SQLException|JsonProcessingException ex) {
      LOG.error("Error selecting taksk", ex);
    }
    return id;
  }

  @Override
  public boolean update(UUID id, TaskDefinition taskDef) {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("UPDATE TASKS SET taskDefinition = ? WHERE ID = ?");
        ) {
      st.setString(1, serializeTaskDef(taskDef));
      st.setString(2, id.toString());
      return st.executeUpdate()>0;
    } catch (SQLException|JsonProcessingException ex) {
      LOG.error("Error selecting taksk", ex);
      return false;
    }
  }

  @Override
  public TaskDefinition read(UUID id) {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM TASKS WHERE ID = ?");
        ) {
      st.setString(1, id.toString());
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        try {
          return deserializeTaskDef(rs.getString("taskDefinition"));
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading task definition", ex);
        }
      }
    } catch (SQLException ex) {
      LOG.error("Error selecting taksk", ex);
    }
    
    return null;
  }

  @Override
  public boolean delete(UUID id) {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("DELETE * FROM TASKS WHERE ID = ?");
        ) {
      st.setString(1, id.toString());
      return st.executeUpdate()>0;
    } catch (SQLException ex) {
      LOG.error("Error selecting taksk", ex);
      return false;
    }
  }

  @Override
  public Collection<Map.Entry<UUID, TaskDefinition>> select() {
    HashMap<UUID, TaskDefinition> map = new HashMap<>();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM TASKS");
        ) {
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        try {
          UUID id = UUID.fromString(rs.getString("id"));
          TaskDefinition td = deserializeTaskDef(rs.getString("taskDefinition"));
          map.put(id, td);
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading task definition", ex);
        }
      }
    } catch (SQLException ex) {
      LOG.error("Error selecting taksk", ex);
    }
    return map.entrySet();
  }
}
