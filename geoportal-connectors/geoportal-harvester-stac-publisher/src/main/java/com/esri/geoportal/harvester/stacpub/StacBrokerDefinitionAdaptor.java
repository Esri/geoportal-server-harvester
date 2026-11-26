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

import static com.esri.geoportal.harvester.stacpub.StacConstants.*;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.CredentialsDefinitionAdaptor;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * THREDDS broker definition adapter.
 */
public class StacBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {
  
  private final CredentialsDefinitionAdaptor credAdaptor;
  private URL hostUrl;
  private String collectionId = "stac";


  /**
   * Creates instance of the adapter.
   * @param def broker definition
   * @throws InvalidDefinitionException if definition is invalid
   * @throws IllegalArgumentException if invalid broker definition
   */
  public StacBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    this.credAdaptor = new CredentialsDefinitionAdaptor(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(StacConnector.TYPE);
    }
    if (!getType().equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      initialize(def);
    }
  }

  protected String getType() {
    return StacConnector.TYPE;
  }
  
  /**
   * Initializes adapter from definition.
   * @param def broker definition
   * @throws InvalidDefinitionException if definition is invalid
   */
  protected void initialize(EntityDefinition def) throws InvalidDefinitionException {
    hostUrl = parseHostUrl(get(P_HOST_URL));
    collectionId = parseCollectionId(get(P_COLLECTION_ID));
  }

  /**
   * Parses and validates host URL.
   * @param sHostUrl host URL as string
   * @return host URL
   * @throws InvalidDefinitionException if host URL is invalid
   */
  private static URL parseHostUrl(String sHostUrl) throws InvalidDefinitionException {
    if (sHostUrl == null) {
      throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL, sHostUrl));
    }
    if (sHostUrl.length() > 500) {
      throw new InvalidDefinitionException("Geoportal host Url length should be less than 500.");
    }
    try {
      return new URL(sHostUrl.endsWith("/") ? sHostUrl : sHostUrl + "/");
    } catch (MalformedURLException ex) {
      throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL, sHostUrl), ex);
    }
  }

  private static String parseCollectionId(String sCollectionId) {
    return StringUtils.defaultIfBlank(sCollectionId, "");
  }

  /**
   * Applies runtime parameter overrides. Host URL and collection id are
   * validated before any state is modified, and the cached fields are
   * refreshed so the getters reflect the effective values.
   * @param params override parameters
   * @throws IllegalArgumentException if overridden host URL is invalid
   */
  @Override
  public void override(Map<String, String> params) {
    URL newHostUrl = hostUrl;
    if (params.containsKey(P_HOST_URL)) {
      try {
        newHostUrl = parseHostUrl(params.get(P_HOST_URL));
      } catch (InvalidDefinitionException ex) {
        throw new IllegalArgumentException(ex.getMessage(), ex);
      }
    }
    String newCollectionId = params.containsKey(P_COLLECTION_ID)
            ? parseCollectionId(params.get(P_COLLECTION_ID))
            : collectionId;

    consume(params,P_HOST_URL);
    consume(params,P_COLLECTION_ID);
    hostUrl = newHostUrl;
    collectionId = newCollectionId;
    credAdaptor.override(params);
  }
  
  public URL getHostUrl() {
    return hostUrl;
  }

  public void setHostUrl(URL hostUrl) {
    this.hostUrl = hostUrl;
    set(P_HOST_URL,hostUrl.toExternalForm());
  }
  

  public String getCollectionId() {
    return this.collectionId;
  }

  public void setCollectionId(String collectionId) {
    this.collectionId = collectionId;
    set(P_COLLECTION_ID,collectionId);
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
