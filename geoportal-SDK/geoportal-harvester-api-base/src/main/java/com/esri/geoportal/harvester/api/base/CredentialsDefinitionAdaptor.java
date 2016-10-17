/*
 * Copyright 2016 Esri, Inc..
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
package com.esri.geoportal.harvester.api.base;

import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_PASSWORD;
import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_USERNAME;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;

/**
 * SimpleCredentials definition adaptor.
 */
public final class CredentialsDefinitionAdaptor extends BrokerDefinitionAdaptor {
  
  /**
   * Creates instance of the adaptor.
   * @param def entity definition
   */
  public CredentialsDefinitionAdaptor(EntityDefinition def) {
    super(def);
  }
  
  /**
   * Gets credentials.
   * @return credentials
   */
  public SimpleCredentials getCredentials() {
    return new SimpleCredentials(get(P_CRED_USERNAME), get(P_CRED_PASSWORD));
  }
  
  /**
   * Sets credentials.
   * @param cred credentials
   */
  public void setCredentials(SimpleCredentials cred) {
    set(P_CRED_USERNAME, cred.getUserName());
    set(P_CRED_PASSWORD, cred.getPassword());
  }

  @Override
  public String toString() {
    return String.format("%s:%s", get(P_CRED_USERNAME), get(P_CRED_PASSWORD));
  }
  
}
