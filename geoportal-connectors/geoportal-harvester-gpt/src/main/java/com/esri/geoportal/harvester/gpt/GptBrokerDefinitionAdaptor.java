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
package com.esri.geoportal.harvester.gpt;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;

/**
 * GPT broker definition adaptor.
 */
public class GptBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {
  public static final String P_HOST_URL        = "gpt-host-url";
  public static final String P_USER_NAME       = "gpt-user-name";
  public static final String P_USER_PASSWORD   = "gpt-user-password";
  
  private URL hostUrl;
  private String userName;
  private String password;

  /**
   * Creates instance of the adaptor.
   * @param def broker definition
   */
  public GptBrokerDefinitionAdaptor(EntityDefinition def) {
    super(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(GptConnector.TYPE);
    } else if (!GptConnector.TYPE.equals(def.getType())) {
      throw new IllegalArgumentException("Broker definition doesn't match");
    } else {
      try {
        hostUrl = new URL(get(P_HOST_URL));
      } catch (MalformedURLException ex) {
        throw new IllegalArgumentException(String.format("Invalid %s: %s", P_HOST_URL,get(P_HOST_URL)), ex);
      }
      userName = get(P_USER_NAME);
      password = get(P_USER_PASSWORD);
    }
  }

  /**
   * Gets host URL.
   * @return host URL
   */
  public URL getHostUrl() {
    return hostUrl;
  }

  /**
   * Sets host URL.
   * @param url host URL
   */
  public void setHostUrl(URL url) {
    this.hostUrl = url;
    set(P_HOST_URL, url.toExternalForm());
  }

  /**
   * Gets user name.
   * @return user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets user name.
   * @param userName user name 
   */
  public void setUserName(String userName) {
    this.userName = userName;
    set(P_USER_NAME, userName);
  }

  /**
   * Gets user password.
   * @return user password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets user password.
   * @param password user password
   */
  public void setPassword(String password) {
    this.password = password;
    set(P_USER_PASSWORD, password);
  }
  
}
