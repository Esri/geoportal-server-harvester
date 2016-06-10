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

import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;

/**
 * SimpleCredentials definition adaptor.
 */
public final class CredentialsDefinitionAdaptor extends BrokerDefinitionAdaptor {
  public static final String P_CRED_USERNAME = "cred-username";
  public static final String P_CRED_PASSWORD = "cred-password";
  
  /**
   * Creates instance of the adaptor.
   * @param def 
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
  
}
