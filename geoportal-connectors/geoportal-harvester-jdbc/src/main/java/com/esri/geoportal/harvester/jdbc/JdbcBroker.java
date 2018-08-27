/*
 * Copyright 2018 Esri, Inc.
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
package com.esri.geoportal.harvester.jdbc;

import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC broker.
 */
public class JdbcBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(JdbcBroker.class);
  
  private final JdbcConnector connector;
  private final JdbcBrokerDefinitionAdaptor definition;
  private Connection connection;
  private PreparedStatement statement;
  private ResultSet resultSet;
  private final List<SqlDataReader> readers = new ArrayList<>();

  public JdbcBroker(JdbcConnector connector, JdbcBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("JDBC",definition.getConnection(),null);
  }
  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  @Override
  public InputConnector getConnector() {
    return connector;
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return true;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    createConnection();
    createStatement();
    createResultSet();
    createReaders();
  }

  @Override
  public void terminate() {
    try {
      if (resultSet != null) {
        resultSet.close();
      }
    } catch(SQLException ex) {
      LOG.warn(String.format("Unexpected error closing result set."), ex);
    }

    try {
      if (statement != null) {
        statement.close();
      }
    } catch (SQLException ex) {
      LOG.warn(String.format("Unexpected error closing statement."), ex);
    }
    
    try {
      if (connection!=null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException ex) {
      LOG.warn(String.format("Unexpected error closing connection to: %s", definition.getConnection()), ex);
    } finally {
      connection = null;
    }
  }
  

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    return new JdbcIterator(iteratorContext);
  }

  @Override
  public DataContent readContent(String id) throws DataInputException {
    // TODO provide implementation for readContent()
    return null;
  }
  
  private void createConnection() throws DataProcessorException {
    try {
      Class.forName(definition.getDriverClass());
      connection = DriverManager.getConnection(definition.getConnection(), definition.getCredentials().getUserName(), definition.getCredentials().getPassword());
    } catch (ClassNotFoundException ex) {
      throw new DataProcessorException(String.format("Error loading JDBC driver class: %s", definition.getDriverClass()), ex);
    } catch (SQLException ex) {
      throw new DataProcessorException(String.format("Error opening JDBC connection to: %s", definition.getConnection()), ex);
    }
  }
  
  private void createStatement() throws DataProcessorException {
    try {
      statement = definition.getSqlStatement().split(" ").length == 1 
              ? connection.prepareStatement(String.format("SELECT * FROM %s", definition.getSqlStatement())) 
              : connection.prepareStatement(definition.getSqlStatement());
    } catch (SQLException ex) {
      throw new DataProcessorException(String.format("Error opening JDBC connection to: %s", definition.getConnection()), ex);
    }
  }
  
  private void createResultSet() throws DataProcessorException {
    try {
      resultSet = statement.executeQuery();
    } catch (SQLException ex) {
      throw new DataProcessorException(String.format("Error opening JDBC connection to: %s", definition.getConnection()), ex);
    }
  }
  
  private void createReaders() throws DataProcessorException {
    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      for (int i=1; i<= metaData.getColumnCount(); i++) {
        final String columnName = metaData.getColumnName(i);
        final int columnType = metaData.getColumnType(i);
        
        SqlDataReader reader = null;
        switch (columnType) {
          case Types.VARCHAR:
          case Types.CHAR:
          case Types.LONGVARCHAR:
          case Types.LONGNVARCHAR:
            reader = (a,r)->a.put(columnName, r.getString(columnName));
            break;
            
          case Types.NVARCHAR:
          case Types.NCHAR:
            reader = (a,r)->a.put(columnName, r.getNString(columnName));
            break;
            
          case Types.DOUBLE:
            reader = (a,r)->a.put(columnName, new Double(r.getDouble(columnName)));
            break;
            
          case Types.FLOAT:
            reader = (a,r)->a.put(columnName, new Double(r.getFloat(columnName)));
            break;
            
          case Types.INTEGER:
          case Types.SMALLINT:
          case Types.TINYINT:
            reader = (a,r)->a.put(columnName, new Double(r.getInt(columnName)));
            break;
            
          case Types.BIGINT:
          case Types.DECIMAL:
          case Types.NUMERIC:
            reader = (a,r)->a.put(columnName, new Double(r.getBigDecimal(columnName).doubleValue()));
            break;
            
          case Types.BOOLEAN:
            reader = (a,r)->a.put(columnName, new Boolean(r.getBoolean(columnName)));
            break;
            
            
          case Types.DATE:
            reader = (a,r)->a.put(columnName, formatIsoDate(r.getDate(columnName)));
            break;
          case Types.TIME:
            reader = (a,r)->a.put(columnName, formatIsoDate(r.getTime(columnName)));
            break;
          case Types.TIMESTAMP:
            reader = (a,r)->a.put(columnName, formatIsoDate(r.getTimestamp(columnName)));
            break;
        }
        
        if (reader != null) {
          readers.add(reader);
        }
      }
    } catch (SQLException ex) {
      throw new DataProcessorException(String.format("Error opening JDBC connection to: %s", definition.getConnection()), ex);
    }
  }
  
  
  /**
   * Formats ISO date.
   * @param date date to format
   * @return ISO date
   */
  private String formatIsoDate(Date date) {
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime);
  }
  
  private interface SqlDataReader {
    void read(Map<String,Object> attributeMap, ResultSet resultSet) throws SQLException;
  }

  private class JdbcIterator implements InputBroker.Iterator {
    private final IteratorContext iteratorContext;

    /**
     * Creates instance of the iterator.
     * @param iteratorContext iterator context
     */
    public JdbcIterator(IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      // TODO provide implementation for hasNext()
      return false;
    }

    @Override
    public DataReference next() throws DataInputException {
      // TODO provide implementation for next()
      return null;
    }
  }
}
