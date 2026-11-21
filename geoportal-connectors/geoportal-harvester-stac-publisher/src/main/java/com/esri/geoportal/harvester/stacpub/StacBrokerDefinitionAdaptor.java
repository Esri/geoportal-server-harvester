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
    } else if (!getType().equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      //initialize(def);
      try {
        String sHostUrl = get(P_HOST_URL);
        if (sHostUrl!=null) {
            if(sHostUrl.length()>500)
            {
                throw new InvalidDefinitionException("Geoportal host Url length should be less tahn 500.");
            }
            sHostUrl = sHostUrl+"/";
        }
        hostUrl = new URL(sHostUrl);
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL,get(P_HOST_URL)), ex);
      }
    }
    collectionId = StringUtils.defaultIfBlank(get(P_COLLECTION_ID), "");
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
    
    try {
      hostUrl = new URL(get(P_HOST_URL));
    } catch (MalformedURLException ex) {
      throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL,get(P_HOST_URL)), ex);
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,P_HOST_URL);
    consume(params,P_COLLECTION_ID);
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
