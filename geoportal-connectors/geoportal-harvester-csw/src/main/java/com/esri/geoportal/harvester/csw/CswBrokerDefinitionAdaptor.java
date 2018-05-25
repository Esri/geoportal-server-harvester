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
package com.esri.geoportal.harvester.csw;

import static com.esri.geoportal.harvester.csw.CswConstants.*;
import com.esri.geoportal.commons.csw.client.IProfile;
import com.esri.geoportal.commons.csw.client.IProfiles;
import com.esri.geoportal.commons.csw.client.impl.ProfilesProvider;
import com.esri.geoportal.commons.robots.BotsConfig;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.base.BotsBrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.CredentialsDefinitionAdaptor;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * CSW definition.
 */
public class CswBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {

  private final BotsBrokerDefinitionAdaptor botsAdaptor;
  private final CredentialsDefinitionAdaptor credAdaptor;
  private URL hostUrl;
  private IProfile profile;
  private String searchText;

  /**
   * Creates instance of the adaptor.
   *
   * @param def broker definition
   * @throws InvalidDefinitionException if invalid definition
   */
  public CswBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    this.credAdaptor = new CredentialsDefinitionAdaptor(def);
    this.botsAdaptor = new BotsBrokerDefinitionAdaptor(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(CswConnector.TYPE);
    } else if (!CswConnector.TYPE.equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      try {
        hostUrl = new URL(get(P_HOST_URL));
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL, get(P_HOST_URL)), ex);
      }
      ProfilesProvider of = new ProfilesProvider();
      IProfiles profiles = of.newProfiles();
      profile = profiles.getProfileById(this.get(P_PROFILE_ID));
      if (profile == null) {
        throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_PROFILE_ID, get(P_PROFILE_ID)));
      }
      searchText = get(P_SEARCH_TEXT);
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,P_HOST_URL);
    consume(params,P_PROFILE_ID);
    consume(params,P_SEARCH_TEXT);
    credAdaptor.override(params);
    botsAdaptor.override(params);
  }

  /**
   * Gets host url.
   *
   * @return host url
   */
  public URL getHostUrl() {
    return hostUrl;
  }

  /**
   * Sets host url.
   *
   * @param url host url
   */
  public void setHostUrl(URL url) {
    this.hostUrl = url;
    set(P_HOST_URL, url.toExternalForm());
  }

  /**
   * Gets profile.
   *
   * @return profile
   */
  public IProfile getProfile() {
    return profile;
  }

  /**
   * Sets profile.
   *
   * @param profile profile
   */
  public void setProfile(IProfile profile) {
    this.profile = profile;
    set(P_PROFILE_ID, profile.getId());
  }

  /**
   * Gets search text.
   * @return search text.
   */
  public String getSearchText() {
    return searchText;
  }

  /**
   * Sets search text.
   * @param searchText search text 
   */
  public void setSearchText(String searchText) {
    this.searchText = searchText;
    set(P_SEARCH_TEXT, searchText);
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

}
