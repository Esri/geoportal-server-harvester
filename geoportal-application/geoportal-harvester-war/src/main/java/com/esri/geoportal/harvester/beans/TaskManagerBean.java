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

import static com.esri.geoportal.harvester.engine.utils.JsonSerializer.serialize;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.engine.managers.TaskManager;
import com.esri.geoportal.harvester.engine.utils.CrudlException;
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
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.esri.geoportal.harvester.engine.utils.JsonSerializer.deserialize;

/**
 * Task manager bean.
 */
@Service
public class TaskManagerBean implements TaskManager {

  private final Logger LOG = LoggerFactory.getLogger(TaskManager.class);

  @Autowired
  private DataSource dataSource;

  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("CREATE TABLE IF NOT EXISTS TASKS ( id varchar(38) PRIMARY KEY, taskDefinition varchar(8192) NOT NULL)");
        ) {
      st.execute();
      LOG.info("TaskManagerBean initialized.");
    } catch (SQLException ex) {
      LOG.info("Error initializing task database", ex);
    }
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    LOG.info(String.format("TaskManagerBean destroyed."));
  }

  @Override
  public UUID create(TaskDefinition taskDef) throws CrudlException {
    UUID id = UUID.randomUUID();
    taskDef.setRef(id.toString());
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("INSERT INTO TASKS (taskDefinition,id) VALUES (?,?)");
        ) {
      st.setString(1, serialize(taskDef));
      st.setString(2, id.toString());
      st.executeUpdate();
    } catch (SQLException|IOException ex) {
      throw new CrudlException("Error selecting task", ex);
    }
    return id;
  }

  @Override
  public boolean update(UUID id, TaskDefinition taskDef) throws CrudlException {
    taskDef.setRef(id.toString());
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("UPDATE TASKS SET taskDefinition = ? WHERE ID = ?");
        ) {
      st.setString(1, serialize(taskDef));
      st.setString(2, id.toString());
      return st.executeUpdate()>0;
    } catch (SQLException|IOException ex) {
      throw new CrudlException("Error selecting task", ex);
    }
  }

  @Override
  public TaskDefinition read(UUID id) throws CrudlException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM TASKS WHERE ID = ?");
        ) {
      st.setString(1, id.toString());
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        try {
          TaskDefinition taskDef = deserialize(rs.getString("taskDefinition"), TaskDefinition.class);
          taskDef.setRef(id.toString());
          return taskDef;
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading task definition", ex);
        }
      }
    } catch (SQLException ex) {
      throw new CrudlException("Error selecting task", ex);
    }
    
    return null;
  }

  @Override
  public boolean delete(UUID id) throws CrudlException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("DELETE FROM TASKS WHERE ID = ?");
        ) {
      st.setString(1, id.toString());
      return st.executeUpdate()>0;
    } catch (SQLException ex) {
      throw new CrudlException("Error selecting task", ex);
    }
  }

  @Override
  public Collection<Map.Entry<UUID, TaskDefinition>> list() throws CrudlException {
    HashMap<UUID, TaskDefinition> map = new HashMap<>();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM TASKS");
        ) {
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        try {
          UUID id = UUID.fromString(rs.getString("id"));
          TaskDefinition td = deserialize(rs.getString("taskDefinition"), TaskDefinition.class);
          td.setRef(id.toString());
          map.put(id, td);
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading task definition", ex);
        }
      }
    } catch (SQLException ex) {
      throw new CrudlException("Error selecting task", ex);
    }
    return map.entrySet();
  }
}
