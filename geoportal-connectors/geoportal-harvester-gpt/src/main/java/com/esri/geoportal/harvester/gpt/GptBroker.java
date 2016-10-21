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
package com.esri.geoportal.harvester.gpt;

import com.esri.geoportal.commons.gpt.client.Client;
import com.esri.geoportal.commons.gpt.client.PublishRequest;
import com.esri.geoportal.commons.gpt.client.PublishResponse;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.BaseProcessInstanceListener;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataException;
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
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GPT broker.
 */
/*package*/ class GptBroker implements OutputBroker {

  private final static Logger LOG = LoggerFactory.getLogger(GptBroker.class);
  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
  private final static String SBOM = generateSBOM();
  private final GptConnector connector;
  private final GptBrokerDefinitionAdaptor definition;
  private final Set<String> existing = new HashSet<>();
  private Client client;
  private volatile boolean preventCleanup;

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
   * @param definition definition
   * @param client client
   */
  public GptBroker(GptConnector connector, GptBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    client = new Client(definition.getHostUrl(), definition.getCredentials());

    if (definition.getCleanup()) {
      context.addListener(new BaseProcessInstanceListener() {
        @Override
        public void onError(DataException ex) {
          preventCleanup = true;
        }
      });
      try {
        List<String> existingIds = client.queryBySource(context.getTask().getDataSource().getBrokerUri().toASCIIString());
        existing.addAll(existingIds);
      } catch (IOException | URISyntaxException ex) {
        throw new DataProcessorException(String.format("Error getting published records for: %s", client), ex);
      }
    }
  }

  @Override
  public void terminate() {
    try {
      if (definition.getCleanup() && !preventCleanup) {
        for (String id : existing) {
          client.delete(id);
        }
        LOG.info(String.format("%d records has been removed during cleanup.", existing.size()));
      }
    } catch (URISyntaxException | IOException ex) {
      LOG.error(String.format("Error terminating broker."), ex);
    } finally {
      try {
        client.close();
      } catch (IOException ex) {
        LOG.error(String.format("Error terminating broker.", ex));
      }
    }
  }

  @Override
  public PublishingStatus publish(DataReference ref) throws DataOutputException {
    try {
      PublishRequest data = new PublishRequest();
      data.src_source_type_s = ref.getBrokerUri().getScheme();
      data.src_source_uri_s = ref.getBrokerUri().toASCIIString();
      data.src_source_name_s = ref.getBrokerName();
      data.src_uri_s = ref.getSourceUri().toASCIIString();
      data.src_lastupdate_dt = ref.getLastModifiedDate() != null ? fromatDate(ref.getLastModifiedDate()) : null;
      
      byte[] content = ref.getContent();
      if (content!=null) {
        data.xml = new String(content, "UTF-8");
        if (data.xml.startsWith(SBOM)) {
          data.xml = data.xml.substring(1);
        }
      }
      
      PublishResponse response = client.publish(data, definition.getForceAdd());
      if (response == null) {
        throw new DataOutputException(this, "No response received");
      }
      if (response.getError() != null) {
        throw new DataOutputException(this, response.getError().getMessage());
      }
      existing.remove(response.getId());
      return response.getStatus().equalsIgnoreCase("created") ? PublishingStatus.CREATED : PublishingStatus.UPDATED;
    } catch (IOException | URISyntaxException ex) {
      throw new DataOutputException(this, "Error publishing data.", ex);
    }
  }

  @Override
  public OutputConnector getConnector() {
    return connector;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  @Override
  public String toString() {
    return String.format("GPT [%s]", definition.getHostUrl());
  }

  private String fromatDate(Date date) {
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    return FORMATTER.format(zonedDateTime);
  }

  private boolean getCleanup() {
    return definition.getCleanup();
  }
}
