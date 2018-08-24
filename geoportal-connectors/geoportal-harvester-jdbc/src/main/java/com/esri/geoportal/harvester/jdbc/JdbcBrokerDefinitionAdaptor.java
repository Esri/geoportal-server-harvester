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

import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * JDBC broker definition adaptor.
 */
public class JdbcBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {
  private String driverClass;
  private String connection;
  private String username;
  private String password;
  private String sqlStatement;

  /**
   * Creates instance of the adaptor.
   *
   * @param def broker definition
   * @throws InvalidDefinitionException if invalid definition
   */
  public JdbcBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(JdbcConnector.TYPE);
    } else if (!JdbcConnector.TYPE.equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      driverClass = get(JdbcConstants.P_JDBC_DRIVER_CLASS);
      connection = get(JdbcConstants.P_JDBC_CONNECTION);
      username = get(JdbcConstants.P_JDBC_USERNAME);
      password = get(JdbcConstants.P_JDBC_PASSWORD);
      sqlStatement = get(JdbcConstants.P_JDBC_SQL_STATEMENT);
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,JdbcConstants.P_JDBC_DRIVER_CLASS);
    consume(params,JdbcConstants.P_JDBC_CONNECTION);
    consume(params,JdbcConstants.P_JDBC_USERNAME);
    consume(params,JdbcConstants.P_JDBC_PASSWORD);
    consume(params,JdbcConstants.P_JDBC_SQL_STATEMENT);
  }

  public String getConnection() {
    return connection;
  }

  public void setConnection(String connection) {
    this.connection = connection;
    set(JdbcConstants.P_JDBC_CONNECTION, connection);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
    set(JdbcConstants.P_JDBC_USERNAME, username);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
    set(JdbcConstants.P_JDBC_PASSWORD, password);
  }

  public String getSqlStatement() {
    return sqlStatement;
  }

  public void setSqlStatement(String sqlStatement) {
    this.sqlStatement = sqlStatement;
    set(JdbcConstants.P_JDBC_SQL_STATEMENT, sqlStatement);
  }

  public String getDriverClass() {
    return driverClass;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
    set(JdbcConstants.P_JDBC_DRIVER_CLASS, driverClass);
  }
}
