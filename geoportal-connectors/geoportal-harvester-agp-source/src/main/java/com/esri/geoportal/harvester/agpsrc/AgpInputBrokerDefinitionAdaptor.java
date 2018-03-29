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
package com.esri.geoportal.harvester.agpsrc;

import com.esri.geoportal.commons.agp.client.AgpClient;
import com.esri.geoportal.commons.agp.client.AgpClient.MetadataFormat;
import static com.esri.geoportal.harvester.agpsrc.AgpConstants.*;
import com.esri.geoportal.commons.robots.BotsConfig;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.base.BotsBrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.CredentialsDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * ArcGIS Portal definition adaptor.
 */
/*package*/ class AgpInputBrokerDefinitionAdaptor  extends BrokerDefinitionAdaptor {

  private final BotsBrokerDefinitionAdaptor botsAdaptor;
  private final CredentialsDefinitionAdaptor credAdaptor;
  
  private URL hostUrl;
  private String folderId;
  private boolean emitXml = true;
  private boolean emitJson = false;
  private MetadataFormat metaFormat = MetadataFormat.DEFAULT;

  /**
   * Creates instance of the adaptor.
   * @param def broker definition
   * @throws IllegalArgumentException if invalid broker definition
   */
  public AgpInputBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    this.credAdaptor =new CredentialsDefinitionAdaptor(def);
    this.botsAdaptor = new BotsBrokerDefinitionAdaptor(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(AgpInputConnector.TYPE);
    } else if (!AgpInputConnector.TYPE.equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      try {
        hostUrl = new URL(get(P_HOST_URL));
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL,get(P_HOST_URL)), ex);
      }
      folderId = get(P_FOLDER_ID);
      emitXml = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(get(P_EMIT_XML)), true);
      emitJson = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(get(P_EMIT_JSON)), false);
      metaFormat = MetadataFormat.parse(get(P_EMIT_XML_FMT), MetadataFormat.DEFAULT);
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,P_HOST_URL);
    consume(params,P_FOLDER_ID);
    consume(params,P_EMIT_XML);
    consume(params,P_EMIT_JSON);
    consume(params,P_EMIT_XML_FMT);
    credAdaptor.override(params);
    botsAdaptor.override(params);
  }
  
  /**
   * Gets host URL.
   * @return host URL
   */
  public URL getHostUrl() {
    return hostUrl;
  }
  
  /**
   * Sets host URL
   * @param url host URL
   */
  public void setHostUrl(URL url) {
    this.hostUrl = url;
    set(P_HOST_URL, url.toExternalForm());
  }

  /**
   * Gets folder id.
   * @return folder id
   */
  public String getFolderId() {
    return folderId;
  }

  /**
   * Sets folder id.
   * @param folderId folder id 
   */
  public void setFolderId(String folderId) {
    this.folderId = folderId;
    set(P_FOLDER_ID, folderId);
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

  /**
   * Gets bots config.
   * @return bots config
   */
  public BotsConfig getBotsConfig() {
    return botsAdaptor.getBotsConfig();
  }

  /**
   * Sets bots config.
   * @param botsConfig bots config 
   */
  public void setBotsConfig(BotsConfig botsConfig) {
    botsAdaptor.setBotsConfig(botsConfig);
  }

  public boolean getEmitXml() {
    return emitXml;
  }

  public void setEmitXml(boolean emitXml) {
    this.emitXml = emitXml;
    set(P_EMIT_XML, BooleanUtils.toStringTrueFalse(emitXml));
  }

  public boolean getEmitJson() {
    return emitJson;
  }

  public void setEmitJson(boolean emitJson) {
    this.emitJson = emitJson;
    set(P_EMIT_JSON, BooleanUtils.toStringTrueFalse(emitJson));
  }

  public MetadataFormat getMetaFormat() {
    return metaFormat;
  }

  public void setMetaFormat(MetadataFormat metaFormat) {
    this.metaFormat = metaFormat;
    set(P_EMIT_XML_FMT, (metaFormat != null ? metaFormat : MetadataFormat.DEFAULT).toString());
  }
  
}
