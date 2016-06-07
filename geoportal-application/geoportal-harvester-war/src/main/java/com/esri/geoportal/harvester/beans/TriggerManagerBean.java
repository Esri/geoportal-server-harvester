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

import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.TriggerManager;
import com.esri.geoportal.harvester.engine.TriggerRegistry;
import com.esri.geoportal.harvester.engine.support.CrudsException;
import static com.esri.geoportal.harvester.engine.support.JsonSerializer.deserialize;
import static com.esri.geoportal.harvester.engine.support.JsonSerializer.serialize;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
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
 * Trigger manager bean.
 */
@Service
public class TriggerManagerBean implements TriggerManager {

  private static final Logger LOG = LoggerFactory.getLogger(TriggerManagerBean.class);
  private final Map<UUID,Trigger.Instance> instances = Collections.synchronizedMap(new HashMap<>());

  @Autowired
  private DataSource dataSource;

  @Autowired
  private TriggerRegistry triggerRegistry;

  @Override
  public Map<UUID,Trigger.Instance> getInstances() {
    return instances;
  }
  
  @Override
  public void removeInstance(Trigger.Instance instance) {
    UUID uuid = getInstances().entrySet().stream()
            .filter(e->e.getValue()==instance)
            .map(e->e.getKey())
            .findFirst()
            .orElse(null);
    if (uuid!=null) {
      getInstances().remove(uuid);
    }
  }

  /**
   * Creates instance of the trigger instance.
   * @param triggerDefinition trigger definition
   * @return trigger instance
   * @throws InvalidDefinitionException if definition is invalid
   */
  public Trigger.Instance createInstance(TriggerDefinition triggerDefinition) throws InvalidDefinitionException {
    Trigger trigger = triggerRegistry.get(triggerDefinition.getType());
    if (trigger != null) {
      return trigger.createInstance(triggerDefinition);
    } else {
      throw new InvalidDefinitionException(String.format("Invalid trigger definition: %s", triggerDefinition));
    }
  }

  @PostConstruct
  public void init() {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("CREATE TABLE IF NOT EXISTS TRIGGERS ( id varchar(38) PRIMARY KEY, definition varchar(1024) NOT NULL)");) {
      st.execute();

      select().stream().forEach((td)->{
        try {
          Trigger.Instance instance = createInstance(td.getValue());
          getInstances().put(td.getKey(), instance);
        } catch (InvalidDefinitionException ex) {
          LOG.warn(String.format("Invalid trigger definiton: %s", td.getValue()), ex);
        }
      });

      LOG.info("TriggerManagerBean initialized.");
    } catch (SQLException|CrudsException ex) {
      LOG.info("Error initializing trigger definition database", ex);
    }
  }

  @Override
  public UUID create(TriggerDefinition data) throws CrudsException {
    UUID id = UUID.randomUUID();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("INSERT INTO TRIGGERS (definition,id) VALUES (?,?)");) {
      Trigger.Instance instance = createInstance(data);
      
      st.setString(1, serialize(data));
      st.setString(2, id.toString());
      st.executeUpdate();
      
      getInstances().put(id, instance);
    } catch (SQLException | IOException | InvalidDefinitionException ex) {
      throw new CrudsException("Error selecting trigger definition", ex);
    }
    return id;
  }

  @Override
  public boolean delete(UUID id) throws CrudsException {
    getInstances().remove(id);
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("DELETE FROM TRIGGERS WHERE ID = ?");) {
      st.setString(1, id.toString());
      return st.executeUpdate() > 0;
    } catch (SQLException ex) {
      throw new CrudsException("Error deleting trigger definition", ex);
    }
  }

  @Override
  public TriggerDefinition read(UUID id) throws CrudsException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM TRIGGERS WHERE ID = ?");) {
      st.setString(1, id.toString());
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        try {
          return deserialize(rs.getString("triggerDefinition"), TriggerDefinition.class);
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading broker definition", ex);
        }
      }
    } catch (SQLException ex) {
      throw new CrudsException("Error selecting broker definition", ex);
    }

    return null;
  }

  @Override
  public Collection<Map.Entry<UUID, TriggerDefinition>> select() throws CrudsException {
    HashMap<UUID, TriggerDefinition> map = new HashMap<>();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM TRIGGERS");) {
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        try {
          UUID id = UUID.fromString(rs.getString("id"));
          TriggerDefinition td = deserialize(rs.getString("triggerDefinition"), TriggerDefinition.class);
          map.put(id, td);
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading broker definition", ex);
        }
      }
    } catch (SQLException ex) {
      throw new CrudsException("Error selecting broker definition", ex);
    }
    return map.entrySet();
  }

  @Override
  public boolean update(UUID id, TriggerDefinition data) throws CrudsException {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("UPDATE TRIGGERS SET triggerDefinition = ? WHERE ID = ?");) {
      st.setString(1, serialize(data));
      st.setString(2, id.toString());
      return st.executeUpdate() > 0;
    } catch (SQLException | IOException ex) {
      throw new CrudsException("Error selecting broker definition", ex);
    }
  }

}
