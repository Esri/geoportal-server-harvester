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

import com.esri.geoportal.commons.stac.client.STACClient;
import com.esri.geoportal.commons.agp.client.FolderEntry;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.BaseProcessInstanceListener;
import com.esri.geoportal.commons.utils.SimpleCredentials;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;


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
  private volatile boolean preventCleanup;

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

      return null;

       
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
    this.client = new STACClient(httpClient, definition.getHostUrl(), definition.getCredentials(), definition.getMaxRedirects());

    if (!context.canCleanup()) {
      preventCleanup = true;
    }
    if (definition.getCleanup() && !preventCleanup) {
      context.addListener(new BaseProcessInstanceListener() {
        @Override
        public void onError(DataException ex) {
          preventCleanup = true;
        }
      });
      try {
        String itemId = "";
        JSONObject search = client.search(itemId);
//        while (search != null) {
//          existing.addAll(Arrays.asList(search.entrySet().toArray()).stream().map(i -> i.id).collect(Collectors.toList()));
//          if (search.nextStart > 0) {
//            search = client.search(String.format("typekeywords:%s", String.format("src_source_uri_s=%s", src_source_uri_s)), 0, search.nextStart, generateToken(1));
//          } else {
//            break;
//          }
//        }
      } catch (Exception ex) {
        throw new DataProcessorException(String.format("Error collecting ids of existing items."), ex);
      }
    }

    try {
      String folderId = StringUtils.trimToNull(definition.getFolderId());
      if (folderId != null) {
        FolderEntry[] folders = null;
        FolderEntry selectedFodler = folders != null
                ? Arrays.stream(folders).filter(folder -> folder.id != null && folder.id.equals(folderId)).findFirst().orElse(
                        Arrays.stream(folders).filter(folder -> folder.title != null && folder.title.equals(folderId)).findFirst().orElse(null)
                )
                : null;
        if (selectedFodler != null) {
          definition.setFolderId(selectedFodler.id);
        } else {
          definition.setFolderId(null);
        }
      } else {
        definition.setFolderId(null);
      }
    } catch (Exception ex) {
      throw new DataProcessorException(String.format("Error listing folders for user: %s", definition.getCredentials().getUserName()), ex);
    }
  }

  @Override
  public void terminate() {
    try {
      if (definition.getCleanup() && !preventCleanup) {
        for (String id : existing) {
          //TO-DO: remove all items added before the user terminated the harvest job
          LOG.debug("Remove " + id);
        }
      }
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

}
