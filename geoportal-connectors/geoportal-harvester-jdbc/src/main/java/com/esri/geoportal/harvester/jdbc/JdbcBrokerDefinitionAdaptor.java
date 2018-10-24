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
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.CredentialsDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * JDBC broker definition adaptor.
 */
/*package*/ class JdbcBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {
  private final CredentialsDefinitionAdaptor credAdaptor;
  private String driverClass;
  private String connection;
  private String sqlStatement;
  private String fileIdColumn;
  private String titleColumn;
  private String descriptionColumn;
  private String types;
  private String script;

  /**
   * Creates instance of the adaptor.
   *
   * @param def broker definition
   * @throws InvalidDefinitionException if invalid definition
   */
  public JdbcBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    this.credAdaptor =new CredentialsDefinitionAdaptor(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(JdbcConnector.TYPE);
    } else if (!JdbcConnector.TYPE.equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      driverClass = get(JdbcConstants.P_JDBC_DRIVER_CLASS);
      connection = get(JdbcConstants.P_JDBC_CONNECTION);
      sqlStatement = get(JdbcConstants.P_JDBC_SQL_STATEMENT);
      fileIdColumn = get(JdbcConstants.P_JDBC_FILEID_COLUMN);
      titleColumn = get(JdbcConstants.P_JDBC_TITLE_COLUMN);
      descriptionColumn = get(JdbcConstants.P_JDBC_DESCRIPTION_COLUMN);
      types = get(JdbcConstants.P_JDBC_TYPES);
      script = get(JdbcConstants.P_JDBC_SCRIPT);
      
      validate();
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,JdbcConstants.P_JDBC_DRIVER_CLASS);
    consume(params,JdbcConstants.P_JDBC_CONNECTION);
    consume(params,JdbcConstants.P_JDBC_SQL_STATEMENT);
    consume(params,JdbcConstants.P_JDBC_FILEID_COLUMN);
    consume(params,JdbcConstants.P_JDBC_TITLE_COLUMN);
    consume(params,JdbcConstants.P_JDBC_DESCRIPTION_COLUMN);
    consume(params,JdbcConstants.P_JDBC_TYPES);
    consume(params,JdbcConstants.P_JDBC_SCRIPT);
    credAdaptor.override(params);
  }

  public String getConnection() {
    return connection;
  }

  public void setConnection(String connection) {
    this.connection = connection;
    set(JdbcConstants.P_JDBC_CONNECTION, connection);
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

  /**
   * Gets credentials.
   * @return credentials
   */
  public SimpleCredentials getCredentials() {
    return credAdaptor.getCredentials();
  }

  /**
   * Sets credentials.
   * @param cred credentials
   */
  public void setCredentials(SimpleCredentials cred) {
    credAdaptor.setCredentials(cred);
  }

  public String getFileIdColumn() {
    return fileIdColumn;
  }

  public void setFileIdColumn(String fileIdColumn) {
    this.fileIdColumn = fileIdColumn;
    set(JdbcConstants.P_JDBC_FILEID_COLUMN, fileIdColumn);
  }

  public String getTitleColumn() {
    return titleColumn;
  }

  public void setTitleColumn(String titleColumn) {
    this.titleColumn = titleColumn;
    set(JdbcConstants.P_JDBC_TITLE_COLUMN, titleColumn);
  }

  public String getDescriptionColumn() {
    return descriptionColumn;
  }

  public void setDescriptionColumn(String descriptionColumn) {
    this.descriptionColumn = descriptionColumn;
    set(JdbcConstants.P_JDBC_DESCRIPTION_COLUMN, descriptionColumn);
  }

  public String getTypes() {
    return types;
  }

  public void setTypes(String types) {
    this.types = types;
    set(JdbcConstants.P_JDBC_TYPES, types);
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
    set(JdbcConstants.P_JDBC_SCRIPT, script);
  }
  
  private void validate() throws InvalidDefinitionException {
    JdbcValidator.validateStatement(getSqlStatement());
  }
  
}
