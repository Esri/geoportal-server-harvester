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

import static com.esri.geoportal.harvester.stacpub.STACConstants.*;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.CredentialsDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * ArcGIS Portal definition adapter.
 */
/*package*/ class STACOutputBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {

  private static final Integer DEFAULT_MAX_REDIRECTS = 5;

  private final CredentialsDefinitionAdaptor credAdaptor;

  private URL hostUrl;
  private String collectionId;
  //private boolean cleanup;
  //private Integer maxRedirects;
  //private boolean uploadFiles;
  //private boolean markdown2html;
  //private boolean useOAuth;
  //private String oAuthToken;

  
  /**
   * Creates instance of the adapter.
   *
   * @param def broker definition
   * @throws IllegalArgumentException if invalid broker definition
   */
  public STACOutputBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    this.credAdaptor = new CredentialsDefinitionAdaptor(def);
    if (credAdaptor.getCredentials().isEmpty()) {
      throw new InvalidDefinitionException("Empty credentials");
    }
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(STACOutputConnector.TYPE);
    } else if (!STACOutputConnector.TYPE.equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      try {
        hostUrl = new URL(get(P_HOST_URL));
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL, get(P_HOST_URL)), ex);
      }
      collectionId = get(P_COLLECTION_ID);
      //cleanup = Boolean.parseBoolean(get(P_FOLDER_CLEANUP));
      //maxRedirects = NumberUtils.toInt(get(P_MAX_REDIRECTS), DEFAULT_MAX_REDIRECTS);
      //uploadFiles = Boolean.parseBoolean(get(P_UPLOAD));
      //markdown2html = Boolean.parseBoolean(get(P_MARKDOWN2HTML));
      //useOAuth = Boolean.parseBoolean(get(P_OAUTH));
      //oAuthToken = get(P_TOKEN);
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params, P_HOST_URL);
    consume(params, P_COLLECTION_ID);
  }

  /**
   * Gets host URL.
   *
   * @return host URL
   */
  public URL getHostUrl() {
    return hostUrl;
  }

  /**
   * Sets host URL
   *
   * @param url host URL
   */
  public void setHostUrl(URL url) {
    this.hostUrl = url;
    set(P_HOST_URL, url.toExternalForm());
  }

  /**
   * Gets folder id.
   *
   * @return folder id
   */
  public String getCollectionId() {
    return collectionId;
  }

  /**
   * Sets folder id.
   *
   * @param collectionId folder id
   */
  public void setCollectionId(String collectionId) {
    this.collectionId = collectionId;
    set(P_COLLECTION_ID, collectionId);
  }

  /**
   * Gets credentials.
   *
   * @return credentials
   */
  public SimpleCredentials getCredentials() {
    return credAdaptor.getCredentials();
  }

  /**
   * Sets credentials.
   *
   * @param cred credentials
   */
  public void setCredentials(SimpleCredentials cred) {
    credAdaptor.setCredentials(cred);
  }
}
