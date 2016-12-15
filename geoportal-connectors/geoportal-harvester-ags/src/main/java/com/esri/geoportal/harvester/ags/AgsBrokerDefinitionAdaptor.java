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
package com.esri.geoportal.harvester.ags;

import com.esri.geoportal.commons.robots.BotsConfig;
import com.esri.geoportal.commons.robots.BotsMode;
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
 * Ags broker definition adaptor.
 */
/*package*/ class AgsBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {
  public static final String P_HOST_URL      = "ags-host-url";
  public static final String P_ENABLE_LAYERS = "ags-enable-layers";
  
  private final BotsBrokerDefinitionAdaptor botsAdaptor;
  private final CredentialsDefinitionAdaptor credAdaptor;
  
  private URL hostUrl;
  private boolean enableLayers;

  /**
   * Creates instance of the adaptor.
   * @param def broker definition
   * @throws IllegalArgumentException if invalid broker definition
   */
  public AgsBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    this.credAdaptor =new CredentialsDefinitionAdaptor(def);
    this.botsAdaptor = new BotsBrokerDefinitionAdaptor(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(AgsConnector.TYPE);
    } else if (!AgsConnector.TYPE.equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      try {
        hostUrl = new URL(get(P_HOST_URL));
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL,get(P_HOST_URL)), ex);
      }
      enableLayers = BooleanUtils.toBoolean(get(P_ENABLE_LAYERS));
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,P_HOST_URL);
    consume(params,P_ENABLE_LAYERS);
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
   * Checks if harvesting layers enabled.
   * @return <code>true</code> if harvesting layers enabled
   */
  public boolean getEnableLayers() {
    return enableLayers;
  }

  /**
   * Allows or disallows to harvest layers.
   * @param enableLayers <code>true</code> if harvesting layers enabled
   */
  public void setEnableLayers(boolean enableLayers) {
    this.enableLayers = enableLayers;
    set(P_ENABLE_LAYERS, BooleanUtils.toStringTrueFalse(enableLayers));
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
   * Gets bots mode.
   * @return bots mode
   */
  public BotsMode getBotsMode() {
    return botsAdaptor.getBotsMode();
  }

  /**
   * Sets bots mode.
   * @param botsMode bots mode 
   */
  public void setBotsMode(BotsMode botsMode) {
    botsAdaptor.setBotsMode(botsMode);
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
  
}
