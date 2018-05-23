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
package com.esri.geoportal.harvester.oai.pmh;

import static com.esri.geoportal.harvester.oai.pmh.OaiConstants.*;
import com.esri.geoportal.commons.robots.BotsConfig;
import com.esri.geoportal.harvester.api.base.BotsBrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * OAI-PMH broker definition adaptor.
 */
/*package*/ class OaiBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {
  
  private final BotsBrokerDefinitionAdaptor botsAdaptor;
  private URL hostUrl;
  private String prefix;
  private String set;


  /**
   * Creates instance of the adaptor.
   * @param def broker definition
   * @throws InvalidDefinitionException if definition is invalid
   * @throws IllegalArgumentException if invalid broker definition
   */
  public OaiBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    this.botsAdaptor = new BotsBrokerDefinitionAdaptor(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(OaiConnector.TYPE);
    } else if (!getType().equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      initialize(def);
    }
  }

  protected String getType() {
    return OaiConnector.TYPE;
  }
  
  /**
   * Initializes adaptor from definition.
   * @param def broker definition
   * @throws InvalidDefinitionException if definition is invalid
   */
  protected void initialize(EntityDefinition def) throws InvalidDefinitionException {
    try {
      hostUrl = new URL(get(P_HOST_URL));
    } catch (MalformedURLException ex) {
      throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL,get(P_HOST_URL)), ex);
    }
    
    prefix = get(P_PREFIX);
    if (prefix==null || prefix.isEmpty()) {
      throw new InvalidDefinitionException(String.format("Undefined %s", P_PREFIX ));
    }
    set = get(P_SET);
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,P_HOST_URL);
    consume(params,P_PREFIX);
    consume(params,P_SET);
    botsAdaptor.override(params);
  }
  
  public URL getHostUrl() {
    return hostUrl;
  }

  public void setHostUrl(URL hostUrl) {
    this.hostUrl = hostUrl;
    set(P_HOST_URL,hostUrl.toExternalForm());
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
    set(P_PREFIX,prefix);
  }

  public String getSet() {
    return set;
  }

  public void setSet(String set) {
    this.set = set;
    set(P_SET,set);
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
