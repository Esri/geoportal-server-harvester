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

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.script.ScriptException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC broker.
 */
/*package*/class JdbcBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(JdbcBroker.class);
  
  private final JdbcConnector connector;
  private final JdbcBrokerDefinitionAdaptor definition;
  private Connection connection;
  private PreparedStatement statement;
  private ResultSet resultSet;
  private final List<SqlStringRetriever> retrievers = new ArrayList<>();
  private final List<SqlDataInserter> inserters = new ArrayList<>();
  private final Map<String,String> columnMappings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  private TaskDefinition td;
  private ScriptProcessor scriptProcessor;

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
    td = context.getTask().getTaskDefinition();
    parseColumnNames();
    createConnection();
    createStatement();
    createResultSet();
    createStringRetrievers();
    createInserters();
    createScriptEngine();
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
  
  private void parseColumnNames() {
    if (definition.getTypes()!=null) {
      Arrays.stream(definition.getTypes().split(",")).forEach(typedef -> {
        List<String> kvp = Arrays.stream(StringUtils.trimToEmpty(typedef).split("=|:")).map(v->StringUtils.trimToEmpty(v)).collect(Collectors.toList());
        if (kvp.size()==2) {
          columnMappings.put(norm(kvp.get(0)), kvp.get(1));
        }
      });
    }
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
  
  private <T> T readValue(ResultSet r, String columnName, Class<T> clazz) throws SQLException {
    switch (clazz.getSimpleName()) {
      case "String":
        return (T)(r.getString(columnName));
      case "Double":
        return (T)(new Double(r.getDouble(columnName)));
      case "Float":
        return (T)(new Float(r.getFloat(columnName)));
      case "Long":
        return (T)(new Long(r.getLong(columnName)));
      case "Integer":
        return (T)(new Integer(r.getInt(columnName)));
      case "Short":
        return (T)(new Integer(r.getShort(columnName)));
      case "BigDecimal":
        return (T)(r.getBigDecimal(columnName));
      case "Boolean":
        return (T)(new Boolean(r.getBoolean(columnName)));
    }
    return null;
  }
  
  private String safeToString(Object obj) {
    return obj!=null? obj.toString(): "";
  }
  
  private SqlStringRetriever createRetriever(final String fieldName, final String columnName, final int columnType) {
    SqlStringRetriever retriever = null;
    
    switch (columnType) {
      case Types.VARCHAR:
      case Types.CHAR:
      case Types.LONGVARCHAR:
      case Types.LONGNVARCHAR:
      case Types.NVARCHAR:
      case Types.NCHAR:
        retriever = (n, r)->n.put(fieldName, readValue(r, columnName, String.class));
        break;

      case Types.DOUBLE:
        retriever = (n, r)->n.put(fieldName, safeToString(readValue(r, columnName, Double.class)));
        break;

      case Types.FLOAT:
        retriever = (n, r)->n.put(fieldName, safeToString(readValue(r, columnName, Float.class)));
        break;

      case Types.INTEGER:
        retriever = (n, r)->n.put(fieldName, safeToString(readValue(r, columnName, Integer.class)));
        break;

      case Types.SMALLINT:
      case Types.TINYINT:
        retriever = (n, r)->n.put(fieldName, safeToString(readValue(r, columnName, Short.class)));
        break;

      case Types.BIGINT:
      case Types.DECIMAL:
      case Types.NUMERIC:
        retriever = (n, r)->n.put(fieldName, safeToString(readValue(r, columnName, BigDecimal.class)));
        break;
    
      case Types.BOOLEAN:
        retriever = (n, r)->n.put(fieldName, safeToString(readValue(r, columnName, Boolean.class)));
        break;

      case Types.DATE:
        retriever = (n, r)->n.put(fieldName, formatIsoDate(r.getDate(columnName)));
        break;
      case Types.TIME:
        retriever = (n, r)->n.put(fieldName, formatIsoDate(r.getTime(columnName)));
        break;
      case Types.TIMESTAMP:
        retriever = (n, r)->n.put(fieldName, formatIsoDate(r.getTimestamp(columnName)));
        break;
        
      case Types.CLOB:
        retriever = (n, r)->n.put(fieldName, formatClob(r.getClob(columnName)));
        break;
        
      case Types.BLOB:
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        retriever = (n, r)->n.put(fieldName, formatBlob(r.getBlob(columnName)));
        break;
    }
    
    return retriever;
  }
  
  private void createStringRetrievers()  throws DataProcessorException {
    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      for (int i=1; i<= metaData.getColumnCount(); i++) {
        final String columnName = metaData.getColumnName(i);
        final int columnType = metaData.getColumnType(i);

        SqlStringRetriever retriever = null;
        if (columnName.equalsIgnoreCase(definition.getFileIdColumn())) {
          retriever = createRetriever("fileid", columnName, columnType);
        } else if (columnName.equalsIgnoreCase(definition.getTitleColumn())) {
          retriever = createRetriever("title", columnName, columnType);
        } else if (columnName.equalsIgnoreCase(definition.getDescriptionColumn())) {
          retriever = createRetriever("description", columnName, columnType);
        }
        
        if (retriever!=null) {
          retrievers.add(retriever);
        }
      }
    } catch (SQLException ex) {
      throw new DataProcessorException(String.format("Error opening JDBC connection to: %s", definition.getConnection()), ex);
    }
  }
  
  private List<String> createAttributeNames(String baseFormat, String columnName) {
    List<String> attributeNames = new ArrayList<>();
    columnName = norm(columnName);
    if (columnMappings.containsKey(columnName)) {
      String desiredFormat = columnMappings.get(columnName);
      if (!desiredFormat.startsWith("_")) {
        attributeNames.add(desiredFormat);
        String suffix = "";
        int dashIndex = baseFormat.lastIndexOf("_");
        if (dashIndex>=0) {
          suffix = baseFormat.substring(dashIndex);
        }
        attributeNames.add((desiredFormat.startsWith("src_")? "": "src_")+desiredFormat.replaceAll("(_txt|_s|_f|_d|_i|_l|_dt|_b)$", "")+suffix);
      } else {
        int dashIndex = baseFormat.lastIndexOf("_");
        if (dashIndex>=0) {
          baseFormat = baseFormat.substring(0, dashIndex);
        }
        attributeNames.add(String.format(baseFormat + desiredFormat, columnName));
      }
    } else {
      attributeNames.add(String.format(baseFormat, columnName));
    }
    return attributeNames;
  }
  
  private List<SqlDataInserter> createInserters(final String columnName, final int columnType) {
    List<SqlDataInserter> inserters = new ArrayList<>();
    
    switch (columnType) {
      case Types.VARCHAR:
      case Types.CHAR:
      case Types.LONGVARCHAR:
      case Types.LONGNVARCHAR:
      case Types.NVARCHAR:
      case Types.NCHAR:
        createAttributeNames("src_%s_txt", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, readValue(r, columnName, String.class))));
        break;

      case Types.DOUBLE:
        createAttributeNames("src_%s_d", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, readValue(r, columnName, Double.class))));
        break;

      case Types.FLOAT:
        createAttributeNames("src_%s_f", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, readValue(r, columnName, Float.class))));
        break;

      case Types.INTEGER:
        createAttributeNames("src_%s_i", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, readValue(r, columnName, Integer.class))));
        break;
        
      case Types.SMALLINT:
      case Types.TINYINT:
        createAttributeNames("src_%s_i", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, readValue(r, columnName, Short.class))));
        break;

      case Types.BIGINT:
      case Types.DECIMAL:
      case Types.NUMERIC:
        createAttributeNames("src_%s_d", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, readValue(r, columnName, BigDecimal.class))));
        break;

      case Types.BOOLEAN:
        createAttributeNames("src_%s_b", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, readValue(r, columnName, Boolean.class))));
        break;


      case Types.DATE:
        createAttributeNames("src_%s_dt", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, formatIsoDate(r.getDate(columnName)))));
        break;
      case Types.TIME:
        createAttributeNames("src_%s_dt", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, formatIsoDate(r.getTime(columnName)))));
        break;
      case Types.TIMESTAMP:
        createAttributeNames("src_%s_dt", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, formatIsoDate(r.getTimestamp(columnName)))));
        break;
        
      case Types.CLOB:
        createAttributeNames("src_%s_txt", norm(columnName)).forEach(
                name -> inserters.add((a,r)->a.put(name, formatClob(r.getClob(columnName)))));
        break;
        
      case Types.BLOB:
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        if (columnMappings.containsKey(norm(columnName)) && columnMappings.get(norm(columnName)).endsWith("_txt")) {
          createAttributeNames("src_%s_txt", norm(columnName)).forEach(
              name -> inserters.add((a,r)->a.put(name, formatBlob(r.getBlob(columnName)))));
        }
        break;
    }
    
    return inserters;
  }
  
  private void createInserters() throws DataProcessorException {
    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      for (int i=1; i<= metaData.getColumnCount(); i++) {
        final String columnName = metaData.getColumnName(i);
        final int columnType = metaData.getColumnType(i);
        
        inserters.addAll(createInserters(columnName, columnType));
      }
    } catch (SQLException ex) {
      throw new DataProcessorException(String.format("Error opening JDBC connection to: %s", definition.getConnection()), ex);
    }
  }
  
  private void createScriptEngine() throws DataProcessorException {
    if (!StringUtils.isBlank(definition.getScript())) {
      scriptProcessor = new ScriptProcessor(definition.getScript());
    }
  }
  
  private String norm(String name) {
    return StringUtils.trimToEmpty(name).replaceAll("\\p{Blank}+|_+", "_").toLowerCase();
  }
  
  private String formatIsoDate(Date date) {
    if (date==null) return null;
    try {
      ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
      return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime);
    } catch (DateTimeException ex) {
      LOG.trace(String.format("Invalid ISO date: %s", date), ex);
      return null;
    }
  }
  
  private String formatClob(Clob clob) {
    if (clob==null) return null;
    try {
      try (Reader r = clob.getCharacterStream();) {
        return IOUtils.toString(r);
      }
    } catch (IOException|SQLException ex) {
      LOG.trace(String.format("Invalid clob."), ex);
      return null;
    }
  }
  
  private String formatBlob(Blob blob) {
    if (blob==null) return null;
    try {
      try (InputStream r = blob.getBinaryStream();) {
        return IOUtils.toString(r, "UTF-8");
      }
    } catch (IOException|SQLException ex) {
      LOG.trace(String.format("Invalid blob."), ex);
      return null;
    }
  }
  
  private DataReference createReference(ResultSet resultSet) throws SQLException, JsonProcessingException, URISyntaxException, UnsupportedEncodingException, ScriptException  {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    Map<String,Object> attr = new HashMap<>();
    
    for (SqlStringRetriever retriever: retrievers) {
      retriever.read(node, resultSet);
    }

    for (SqlDataInserter reader: inserters) {
      reader.read(attr, resultSet);
    }

    String nodeAsJson = null;
    String id = node.get("fileid").textValue();
    SimpleDataReference ref = new SimpleDataReference(getBrokerUri(), getEntityDefinition().getLabel(), id, null, new URI("uuid", id, null), td.getSource().getRef(), td.getRef());
    
    if (scriptProcessor != null) {
      Map data = mapper.convertValue(node, Map.class);
      Map[] result = scriptProcessor.process(data, attr);
      nodeAsJson = mapper.writeValueAsString(result[0]);
      attr = result[1];
    } else {
      nodeAsJson = mapper.writeValueAsString(node);
    }
    
    ref.addContext(MimeType.APPLICATION_JSON, nodeAsJson.getBytes("UTF-8"));
    ref.getAttributesMap().putAll(attr);

    return ref;
  }
  
  private interface SqlStringRetriever {
    void read(ObjectNode node, ResultSet resultSet) throws SQLException;
  }
  
  private interface SqlDataInserter {
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
      try {
        return resultSet.next();
      } catch (SQLException ex) {
        throw new DataInputException(JdbcBroker.this, String.format("Error iterating data."), ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      try {
        return createReference(resultSet);
      } catch (SQLException|URISyntaxException|UnsupportedEncodingException|JsonProcessingException|ScriptException ex) {
        throw new DataInputException(JdbcBroker.this, String.format("Error reading data"), ex);
      }
    }
  }
}
