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
import com.esri.geoportal.commons.stac.client.Client;
import com.esri.geoportal.commons.stac.client.PublishRequest;
import com.esri.geoportal.commons.stac.client.PublishResponse;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * STAC output broker.
 */
/*package*/ class StacOutputBroker implements OutputBroker {

    public static final String DEFAULT_COLLECTIONS_FIELD_NAME = "src_collections_s";
    private static final Logger LOG = LoggerFactory.getLogger(StacOutputBroker.class);
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final static String SBOM = generateSBOM();
    
    private final StacConnector connector;
    private final StacBrokerDefinitionAdaptor definition;
    private Client client;
    private CloseableHttpClient httpClient;
    private final Set<String> existing = new HashSet<>();

    private static String generateSBOM() {
      try {
        return new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        LOG.error(String.format("Error creating BOM."), ex);
        return "";
      }
    }
  
  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition
   */
  public StacOutputBroker(StacConnector connector, StacBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  
    @Override
    public void initialize(InitContext context) throws DataProcessorException {
        definition.override(context.getParams());
        this.httpClient = HttpClientBuilder.create().useSystemProperties().setRedirectStrategy(LaxRedirectStrategy.INSTANCE).build();

        client = new Client(definition.getHostUrl(), 
                definition.getCredentials(),
                StringUtils.defaultIfBlank(definition.getCollectionId(), DEFAULT_COLLECTIONS_FIELD_NAME));

    }

    @Override
    public PublishingStatus publish(DataReference ref) throws DataOutputException {
        try {
            Object ownerObj = ref.getAttributesMap().get("owner");
            String owner = ownerObj instanceof String ? (String) ownerObj : null;

            Object uuidObj = ref.getAttributesMap().get("uuid");
            String uuid = uuidObj instanceof UUID ? ((UUID) uuidObj).toString().replaceAll("[\\{\\}-]", "") : null;

            PublishRequest data = new PublishRequest();
            data.src_source_type_s = ref.getBrokerUri().getScheme();
            data.src_source_uri_s = ref.getBrokerUri().toASCIIString();
            data.src_source_name_s = ref.getBrokerName();
            data.src_uri_s = ref.getSourceUri().toASCIIString();
            data.src_lastupdate_dt = ref.getLastModifiedDate() != null ? formatDate(ref.getLastModifiedDate()) : null;
            data.sys_owner_s = owner;
            data.sys_owner_txt = owner;
            data.src_source_ref_s = ref.getInputBrokerRef()!=null? ref.getInputBrokerRef().replaceAll("-", ""): null;
            data.src_task_ref_s = ref.getTaskRef()!=null? ref.getTaskRef().replaceAll("-", ""): null;

            String json = "";
            byte[] content = ref.getContent(MimeType.APPLICATION_JSON);
            if (content != null) {
              json = new String(content, "UTF-8");
              if (json.startsWith(SBOM)) {
                json = json.substring(1);
              }
            }

            PublishResponse response = client.publishItem(data, ref.getAttributesMap(), json);
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
        } catch (IOException ex) {
          throw new DataOutputException(this, ref, String.format("Error publishing data: %s Exception: "+ex, ref), ex);
        } catch (URISyntaxException ex) {
            throw new DataOutputException(this, ref, "Invalid publishing URI", ex);
        }
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
  public void terminate() {
    try {
      if (client!=null) {
        client.close();
      }
    } catch ( IOException ex) {
      LOG.error(String.format("Error terminating broker."), ex);
    }
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    // return definition.getCredentials() == null || definition.getCredentials().isEmpty() ? true : definition.getCredentials().equals(creds);
    return true;
  }

  private String formatDate(Date date) {
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    return FORMATTER.format(zonedDateTime);
  }
}
