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
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * GPT broker.
 */
/*package*/ class GptBroker implements OutputBroker {
  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
  private final GptConnector connector;
  private final GptBrokerDefinitionAdaptor definition;
  private final Client client;

  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   * @param client client
   */
  public GptBroker(GptConnector connector, GptBrokerDefinitionAdaptor definition, Client client) {
    this.connector = connector;
    this.definition = definition;
    this.client = client;
  }

  @Override
  public PublishingStatus publish(DataReference ref) throws DataOutputException {
    try {
      PublishRequest data = new PublishRequest();
      data.src_source_type_s = ref.getBrokerUri().getScheme();
      data.src_source_uri_s = ref.getBrokerUri().toASCIIString();
      data.src_uri_s = ref.getSourceUri().toASCIIString();
      data.src_lastupdate_dt = ref.getLastModifiedDate()!=null? fromatDate(ref.getLastModifiedDate()): null;
      data.xml = new String(ref.getContent(),"UTF-8");
      PublishResponse response = client.publish(data, definition.getForceAdd());
      if (response==null) {
        throw new DataOutputException(this, "No response received");
      }
      if (response.getError()!=null) {
        throw new DataOutputException(this, response.getError().getMessage());
      }
      return response.getStatus().equalsIgnoreCase("created")? PublishingStatus.created: PublishingStatus.updated;
    } catch (IOException|URISyntaxException ex) {
      throw new DataOutputException(this, "Error publishing data.", ex);
    }
  }

  @Override
  public void close() throws IOException {
    client.close();
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
    ZonedDateTime ofInstant = ZonedDateTime.ofInstant(zonedDateTime.toInstant(), ZoneOffset.UTC);
    return FORMATTER.format(zonedDateTime);
  }
  
}
