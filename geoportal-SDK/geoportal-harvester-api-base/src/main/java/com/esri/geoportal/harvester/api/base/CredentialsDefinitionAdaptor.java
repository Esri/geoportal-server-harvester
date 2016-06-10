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

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import org.apache.commons.lang3.StringUtils;

/**
 * Credentials definition adaptor.
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
  public Credentials getCredentials() {
    return new Credentials(get(P_CRED_USERNAME), get(P_CRED_PASSWORD));
  }
  
  /**
   * Sets credentials.
   * @param cred credentials
   */
  public void setCredentials(Credentials cred) {
    set(P_CRED_USERNAME, cred.getUserName());
    set(P_CRED_PASSWORD, cred.getPassword());
  }
  
  /**
   * Credentials.
   */
  public static final class Credentials {
    private final String userName;
    private final String password;

    /**
     * Creates instance of the credentials.
     * @param userName user name
     * @param password password
     */
    public Credentials(String userName, String password) {
      this.userName = userName;
      this.password = password;
    }

    /**
     * Gets user name.
     * @return user name
     */
    public String getUserName() {
      return userName;
    }

    /**
     * Gets password.
     * @return password
     */
    public String getPassword() {
      return password;
    }
    
    /**
     * Checks if credentials are empty.
     * Empty credentials have user name or password or both empty.
     * @return <code>true</code> if empty.
     */
    public boolean isEmpty() {
      return StringUtils.trimToEmpty(userName).isEmpty() || StringUtils.trimToEmpty(password).isEmpty();
    }
    
    @Override
    public String toString() {
      return String.format("%s:%s", userName, password);
    }
  }
}
