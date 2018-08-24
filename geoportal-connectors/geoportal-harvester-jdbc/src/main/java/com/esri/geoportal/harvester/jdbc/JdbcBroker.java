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
import java.sql.SQLException;
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
    try {
      Class.forName(definition.getDriverClass());
      connection = DriverManager.getConnection(definition.getConnection(), definition.getCredentials().getUserName(), definition.getCredentials().getPassword());
    } catch (ClassNotFoundException ex) {
      throw new DataProcessorException(String.format("Error loading JDBC driver class: %s", definition.getDriverClass()), ex);
    } catch (SQLException ex) {
      throw new DataProcessorException(String.format("Error opening JDBC connection to: %s", definition.getConnection()), ex);
    }
  }

  @Override
  public void terminate() {
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
