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

import com.esri.geoportal.harvester.api.n.BrokerDefinition;
import com.esri.geoportal.harvester.api.n.InvalidDefinitionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * GPT definition.
 */
public class GptDefinition extends BrokerDefinition {
  public static final String TYPE = "GPT";
  
  public static final String P_HOST_URL        = "gpt.host.url";
  public static final String P_USER_NAME       = "gpt.user.name";
  public static final String P_USER_PASSWORD   = "gpt.user.password";
  
  private URL hostUrl;
  private String userName;
  private String password;

  @Override
  public String getType() {
    return TYPE;
  }

  public GptDefinition validate() throws InvalidDefinitionException {
    if (getHostUrl() == null) {
      throw new InvalidDefinitionException(String.format("Invalid host url: %s", this.get(P_HOST_URL)));
    }
    return this;
  }

  public URL getHostUrl() {
    if (hostUrl == null) {
      try {
        hostUrl = new URL(this.get(P_HOST_URL));
      } catch (MalformedURLException | NullPointerException ex) {
      }
    }
    return hostUrl;
  }

  public void setHostUrl(URL url) {
    this.hostUrl = url;
    this.put(P_HOST_URL, url.toExternalForm());
  }

  public String getUserName() {
    if (userName == null) {
        userName = this.get(P_USER_NAME);
    }
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
    this.put(P_USER_NAME, userName);
  }

  public String getPassword() {
    if (password == null) {
        password = this.get(P_USER_PASSWORD);
    }
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
    this.put(P_USER_PASSWORD, password);
  }
  
}
