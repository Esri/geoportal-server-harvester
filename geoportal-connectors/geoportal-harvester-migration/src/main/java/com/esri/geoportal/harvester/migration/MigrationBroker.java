/*
 * Copyright 2017 Esri, Inc.
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
package com.esri.geoportal.harvester.migration;

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.engine.services.Engine;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migration broker.
 */
/*package*/ class MigrationBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(MigrationBroker.class);
  private static final int PAGE_SIZE = 10;

  private final MigrationConnector connector;
  private final MigrationBrokerDefinitionAdaptor definition;
  
  private DataSource dataSource;
  private final Map<Integer,String> userMap = new HashMap<>();
  private final Map<String,MigrationHarvestSite> sites = new HashMap<>();
  private final ArrayList<MigrationIterator> iters = new ArrayList<>();
  
  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   */
  public MigrationBroker(MigrationConnector connector, MigrationBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("MIG",definition.getJndi(),null);
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    try {
      MigrationIterator iter = new MigrationIterator(definition, getBrokerUri(), iteratorContext);
      iters.add(iter);
      return iter;
    } catch(URISyntaxException ex) {
      throw new DataInputException(this, String.format("Error creating iterator."), ex);
    }
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
  public void initialize(InitContext context) throws DataProcessorException {
    try {
      initDataSource(context);
      buildUserMap(context);
      buildHarvestSites(context);
    } catch (NamingException|SQLException ex) {
      throw new DataProcessorException(String.format("Unable to init broker."), ex);
    }
  }

  @Override
  public void terminate() {
    iters.forEach(MigrationIterator::close);
    iters.clear();
  }
  
  private Engine getEngine() {
    Engine e = Engine.ENGINES.get("DEFAULT");
    if (e==null) {
      e = Engine.ENGINES.values().stream().findFirst().orElse(null);
    }
    return e;
  }
  
  private void initDataSource(InitContext context) throws NamingException {
    Context initContext = new InitialContext();
    Context webContext = (Context)initContext.lookup("java:/comp/env");
    dataSource = (DataSource) webContext.lookup(definition.getJndi());  
  }
  
  private void buildUserMap(InitContext context) throws SQLException {
    try (
            Connection conn = dataSource.getConnection(); 
            PreparedStatement st = makeUserStatement(conn);
            ResultSet rs = st.executeQuery();
        ) {
      while (rs.next()) {
        Integer userId = rs.getInt("USERID");
        String userName = rs.getString("USERNAME");
        userMap.put(userId, userName);
      }
    }
  }
  
  private PreparedStatement makeUserStatement(Connection conn) throws SQLException {
    return conn.prepareStatement("SELECT * FROM GPT_USER");
  }
  
  private void buildHarvestSites(InitContext context) throws SQLException {
    try (
            Connection conn = dataSource.getConnection(); 
            PreparedStatement st = makeSitesStatement(conn);
            ResultSet rs = st.executeQuery();
        ) {
      MigrationSiteBuilder builder = new MigrationSiteBuilder(getEngine());
      while (rs.next()) {
        MigrationHarvestSite hs = new MigrationHarvestSite();
        
        hs.docuuid = StringUtils.trimToEmpty(rs.getString("DOCUUID"));
        hs.title = StringUtils.trimToEmpty(rs.getString("TITLE"));
        hs.type = StringUtils.trimToEmpty(rs.getString("PROTOCOL_TYPE")).toUpperCase();
        hs.host = StringUtils.trimToEmpty(rs.getString("HOST_URL"));
        hs.protocol = rs.getString("PROTOCOL");
        hs.frequency = MigrationHarvestSite.Frequency.parse(StringUtils.trimToEmpty(rs.getString("FREQUENCY")));
        
        sites.put(hs.docuuid, hs);
        
        builder.buildSite(hs);
      }
    }
  }
  
  private PreparedStatement makeSitesStatement(Connection conn) throws SQLException {
    return conn.prepareStatement("SELECT * FROM GPT_RESOURCE WHERE PROTOCOL IS NOT NULL");
  }
  
  private class MigrationIterator implements InputBroker.Iterator {
    private final IteratorContext iteratorContext;
    private final MigrationDataBuilder dataBuilder;
    
    private Connection conn;
    private PreparedStatement adminSt;
    private ResultSet adminRs;
    
    private MigrationData data;

    public MigrationIterator(MigrationBrokerDefinitionAdaptor definition, URI brokerUri, IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
      this.dataBuilder = new MigrationDataBuilder(definition, brokerUri, userMap, sites);
    }

    @Override
    public boolean hasNext() throws DataInputException {
      try {
        initResultSet();
        if (data!=null) return true;
        if (!adminRs.next()) {
          return false;
        }
        
        MigrationData dt = new MigrationData();
        
        dt.docuuid = StringUtils.trimToEmpty(adminRs.getString("DOCUUID"));
        dt.title = StringUtils.trimToEmpty(adminRs.getString("TITLE"));
        dt.owner = adminRs.getInt("OWNER");
        dt.updateDate = adminRs.getTimestamp("UPDATEDATE");
        dt.fileidentifier = StringUtils.trimToEmpty(adminRs.getString("FILEIDENTIFIER"));
        dt.pubmethod = StringUtils.trimToEmpty(adminRs.getString("PUBMETHOD"));
        dt.sourceuri = StringUtils.trimToEmpty(adminRs.getString("SOURCEURI"));
        dt.siteuuid = StringUtils.trimToEmpty(adminRs.getString("SITEUUID"));
        
        data = dt;
        
        return true;
      } catch (SQLException ex) {
        throw new DataInputException(MigrationBroker.this, String.format("Error fetching data."), ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      if (data==null) {
        throw new DataInputException(MigrationBroker.this, String.format("No more data available"));
      }
      MigrationData dt = data;
      data = null;
      
      try (PreparedStatement st = createDataStatement(dt.docuuid); ResultSet rs = st.executeQuery();) {
        if (rs.next()) {
          String xml = rs.getString("XML");
          DataReference ref = dataBuilder.buildReference(dt, xml);
          return ref;
        } else {
          throw new DataInputException(MigrationBroker.this, String.format("No more data available"));
        }
      } catch (DataInputException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new DataInputException(MigrationBroker.this, String.format("Error reading data."), ex);
      }
    }
    
    private PreparedStatement createDataStatement(String id) throws SQLException {
      PreparedStatement st = conn.prepareStatement("SELECT * FROM GPT_RESOURCE_DATA WHERE DOCUUID = ?");
      st.setString(1, id);
      return st;
    }
    
    public void close() {
      if (adminRs!=null) {
        try {
          adminRs.close();
        } catch(SQLException ex) {
          LOG.warn(String.format("Error closing iterator."), ex);
        }
      }
      if (adminSt!=null) {
        try {
          adminSt.close();
        } catch(SQLException ex) {
          LOG.warn(String.format("Error closing iterator."), ex);
        }
      }
      if (conn!=null) {
        try {
          conn.close();
        } catch(SQLException ex) {
          LOG.warn(String.format("Error closing iterator."), ex);
        }
      }
    }
    
    private void initResultSet() throws SQLException {
      if (conn==null) {
        conn = dataSource.getConnection();
      }
      if (adminSt==null) {
        adminSt = conn.prepareStatement("SELECT * FROM GPT_RESOURCE WHERE APPROVALSTATUS IN ('APPROVED','REVIEWED') AND PROTOCOL IS NULL");
      }
      if (adminRs==null) {
        adminRs = adminSt.executeQuery();
      }
    }
    
  }
}
