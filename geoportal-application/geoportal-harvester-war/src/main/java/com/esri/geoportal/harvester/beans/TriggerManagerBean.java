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

import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.utils.CrudlException;
import static com.esri.geoportal.harvester.engine.utils.JsonSerializer.serialize;
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
import javax.annotation.PreDestroy;
import static com.esri.geoportal.harvester.engine.utils.JsonSerializer.deserialize;

/**
 * Trigger manager bean.
 */
@Service
public class TriggerManagerBean implements TriggerManager {

  private static final Logger LOG = LoggerFactory.getLogger(TriggerManagerBean.class);

  @Autowired
  private DataSource dataSource;

  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("CREATE TABLE IF NOT EXISTS TRIGGERS ( id varchar(38) PRIMARY KEY, definition varchar(8192) NOT NULL)");) {
      st.execute();
      LOG.info("TriggerManagerBean initialized.");
    } catch (SQLException ex) {
      LOG.info("Error initializing trigger definition database", ex);
    }
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    LOG.info(String.format("TriggerManagerBean destroyed."));
  }

  @Override
  public UUID create(TaskUuidTriggerDefinitionPair data) throws CrudlException {
    UUID id = UUID.randomUUID();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("INSERT INTO TRIGGERS (definition,id) VALUES (?,?)");) {
      st.setString(1, serialize(data));
      st.setString(2, id.toString());
      st.executeUpdate();
    } catch (SQLException | IOException ex) {
      throw new CrudlException("Error selecting trigger definition", ex);
    }
    return id;
  }

  @Override
  public boolean delete(UUID id) throws CrudlException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("DELETE FROM TRIGGERS WHERE ID = ?");) {
      st.setString(1, id.toString());
      return st.executeUpdate() > 0;
    } catch (SQLException ex) {
      throw new CrudlException("Error deleting trigger definition", ex);
    }
  }

  @Override
  public TaskUuidTriggerDefinitionPair read(UUID id) throws CrudlException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM TRIGGERS WHERE ID = ?");) {
      st.setString(1, id.toString());
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        try {
          return deserialize(rs.getString("definition"), TaskUuidTriggerDefinitionPair.class);
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading broker definition", ex);
        }
      }
    } catch (SQLException ex) {
      throw new CrudlException("Error selecting broker definition", ex);
    }

    return null;
  }

  @Override
  public Collection<Map.Entry<UUID, TaskUuidTriggerDefinitionPair>> list() throws CrudlException {
    HashMap<UUID, TaskUuidTriggerDefinitionPair> map = new HashMap<>();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM TRIGGERS");) {
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        try {
          UUID id = UUID.fromString(rs.getString("id"));
          TaskUuidTriggerDefinitionPair td = deserialize(rs.getString("definition"), TaskUuidTriggerDefinitionPair.class);
          map.put(id, td);
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading broker definition", ex);
        }
      }
    } catch (SQLException ex) {
      throw new CrudlException("Error selecting broker definition", ex);
    }
    return map.entrySet();
  }

  @Override
  public boolean update(UUID id, TaskUuidTriggerDefinitionPair data) throws CrudlException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("UPDATE TRIGGERS SET definition = ? WHERE ID = ?");) {
      st.setString(1, serialize(data));
      st.setString(2, id.toString());
      return st.executeUpdate() > 0;
    } catch (SQLException | IOException ex) {
      throw new CrudlException("Error selecting broker definition", ex);
    }
  }

}
