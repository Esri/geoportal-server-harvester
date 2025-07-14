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
package com.esri.geoportal.harvester.stacpub;

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.stac.client.STACClient;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.BaseProcessInstanceListener;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.commons.stac.client.PublishRequest;
import com.esri.geoportal.commons.stac.client.PublishResponse;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * ArcGIS Portal output broker.
 */
/*package*/ class STACOutputBroker implements OutputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(STACOutputBroker.class);
  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  private final STACOutputConnector connector;
  private final STACOutputBrokerDefinitionAdaptor definition;
  private CloseableHttpClient httpClient;
  private STACClient client;
  private String token;
  private final Set<String> existing = new HashSet<>();
  private final static String SBOM = generateSBOM();
  
  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition
   */
  public STACOutputBroker(STACOutputConnector connector, STACOutputBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public PublishingStatus publish(DataReference ref) throws DataOutputException {
    
    try {

      // build typeKeywords array
      String src_source_type_s = URLEncoder.encode(ref.getBrokerUri().getScheme(), "UTF-8");
      String src_source_uri_s = URLEncoder.encode(ref.getBrokerUri().toASCIIString(), "UTF-8");
      String src_source_name_s = URLEncoder.encode(ref.getBrokerName(), "UTF-8");
      String src_uri_s = URLEncoder.encode(ref.getSourceUri().toASCIIString(), "UTF-8");
      String src_lastupdate_dt = ref.getLastModifiedDate() != null ? URLEncoder.encode(formatDate(ref.getLastModifiedDate()), "UTF-8") : null;

      String json = null;
      byte[] content = null;
      try {
        content = ref.getContent(MimeType.APPLICATION_JSON);
      } catch (IOException ex) {
        java.util.logging.Logger.getLogger(STACOutputBroker.class.getName()).log(Level.SEVERE, null, ex);
      }
      if (content != null) {
        json = new String(content, "UTF-8");
        if (json.startsWith(SBOM)) {
          json = json.substring(1);
        }
      }
      JSONParser p = new JSONParser();
      JSONObject stacItem = null;
      try {
        stacItem = (JSONObject) p.parse(json);
      } catch (ParseException ex) {
        java.util.logging.Logger.getLogger(STACOutputBroker.class.getName()).log(Level.SEVERE, null, ex);
      }
      
      // TO-DO: insert source info into JSON?
      PublishResponse response = client.publish(stacItem, this.definition.getCollectionId());
      if (response == null) {
        throw new DataOutputException(this, ref, "No response received");
      }
      if (response.getError() != null) {
        throw new DataOutputException(this, ref, response.getError().getMessage()+"Source URI: "+ref.getSourceUri()) {
          @Override
          public boolean isNegligible() {
            return true;
          }
        };
      }
      existing.remove(response.getId());
      return response.getStatus().equalsIgnoreCase("created") ? PublishingStatus.CREATED : PublishingStatus.UPDATED;

       
    } catch (UnsupportedEncodingException ex) {
      return null;
    }    
  }
  
    
  private String deleteItem(String itemId, String collectionId) throws URISyntaxException, IOException {
    if (token == null) {
//      token = generateToken();
    }
    String response = deleteItem(itemId, collectionId, token);
    return response;
  }

  private String deleteItem(String itemId, String collectionId, String token) throws URISyntaxException, IOException {
    return client.deleteItem(itemId, collectionId, token);
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  @Override
  public OutputConnector getConnector() {
    return connector;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    this.httpClient = HttpClientBuilder.create().useSystemProperties().setRedirectStrategy(LaxRedirectStrategy.INSTANCE).build();
    this.client = new STACClient(httpClient, definition.getHostUrl(), definition.getCredentials());

    try {
      String collectionId = StringUtils.trimToNull(definition.getCollectionId());
      if (collectionId != null) {
        // TO-DO: see if collectionId exists in the STAC
      } else {
        definition.setCollectionId(null);
      }
    } catch (Exception ex) {
      throw new DataProcessorException(String.format("Error listing folders for user: %s", definition.getCredentials().getUserName()), ex);
    }
  }

  @Override
  public void terminate() {
    try {
      if (client!=null) {
        client.close();
      }
    } catch (IOException ex) {
      LOG.error(String.format("Error terminating broker."), ex);
    }
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return definition.getCredentials() == null || definition.getCredentials().isEmpty() ? true : definition.getCredentials().equals(creds);
  }

  private String formatDate(Date date) {
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    return FORMATTER.format(zonedDateTime);
  }

  private static String generateSBOM() {
    try {
      return new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      LOG.error(String.format("Error creating BOM."), ex);
      return "";
    }
  }


}
