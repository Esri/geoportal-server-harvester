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

import com.esri.geoportal.harvester.api.EntityDefinition;
import com.esri.geoportal.harvester.engine.BrokerDefinitionManager;
import static com.esri.geoportal.harvester.support.BrokerDefinitionSerializer.deserializeBrokerDef;
import static com.esri.geoportal.harvester.support.BrokerDefinitionSerializer.serializeBrokerDef;
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
 * Broker definition manager bean.
 */
@Service
public class BrokerDefinitionManagerBean implements BrokerDefinitionManager {

  private static final Logger LOG = LoggerFactory.getLogger(BrokerDefinitionManager.class);

  @Autowired
  private DataSource dataSource;

  /**
   * Initializes database.
   */
  @PostConstruct
  public void init() {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("CREATE TABLE IF NOT EXISTS BROKERS ( id varchar(38) PRIMARY KEY, brokerDefinition varchar(1024) NOT NULL)");
        ) {
      st.execute();
      LOG.info("BrokerDefinitionManagerBean initialized.");
    } catch (SQLException ex) {
      LOG.info("Error initializing broker definition database", ex);
    }
  }

  @Override
  public UUID create(EntityDefinition brokerDef) {
    UUID id = UUID.randomUUID();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("INSERT INTO BROKERS (brokerDefinition,id) VALUES (?,?)");
        ) {
      st.setString(1, serializeBrokerDef(brokerDef));
      st.setString(2, id.toString());
      st.executeUpdate();
    } catch (SQLException|JsonProcessingException ex) {
      LOG.error("Error selecting broker definition", ex);
    }
    return id;
  }

  @Override
  public boolean update(UUID id, EntityDefinition brokerDef) {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("UPDATE BROKERS SET brokerDefinition = ? WHERE ID = ?");
        ) {
      st.setString(1, serializeBrokerDef(brokerDef));
      st.setString(2, id.toString());
      return st.executeUpdate()>0;
    } catch (SQLException|JsonProcessingException ex) {
      LOG.error("Error selecting broker definition", ex);
      return false;
    }
  }

  @Override
  public EntityDefinition read(UUID id) {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM BROKERS WHERE ID = ?");
        ) {
      st.setString(1, id.toString());
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        try {
          return deserializeBrokerDef(rs.getString("brokerDefinition"));
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading broker definition", ex);
        }
      }
    } catch (SQLException ex) {
      LOG.error("Error selecting broker definition", ex);
    }
    
    return null;
  }

  @Override
  public boolean delete(UUID id) {
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("DELETE FROM BROKERS WHERE ID = ?");
        ) {
      st.setString(1, id.toString());
      return st.executeUpdate()>0;
    } catch (SQLException ex) {
      LOG.error("Error selecting broker definition", ex);
      return false;
    }
  }

  @Override
  public Collection<Map.Entry<UUID, EntityDefinition>> select() {
    HashMap<UUID, EntityDefinition> map = new HashMap<>();
    try (
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM BROKERS");
        ) {
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        try {
          UUID id = UUID.fromString(rs.getString("id"));
          EntityDefinition td = deserializeBrokerDef(rs.getString("brokerDefinition"));
          map.put(id, td);
        } catch (IOException | SQLException ex) {
          LOG.warn("Error reading broker definition", ex);
        }
      }
    } catch (SQLException ex) {
      LOG.error("Error selecting broker definition", ex);
    }
    return map.entrySet();
  }
  
}
