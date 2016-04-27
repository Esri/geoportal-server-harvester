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
package com.esri.geoportal.harvester.gpt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * GPT attributes adaptor.
 */
public class GptAttributesAdaptor extends AbstractMap<String,String>  {
  public static final String P_HOST_URL        = "gpt.host.url";
  public static final String P_USER_NAME       = "gpt.user.name";
  public static final String P_USER_PASSWORD   = "gpt.user.password";
  
  private final Map<String,String> attributes;
  
  private URL hostUrl;
  private String userName;
  private String password;

  /**
   * Creates instance of the adaptor.
   * @param attributes attributes
   */
  public GptAttributesAdaptor(Map<String, String> attributes) {
    this.attributes = attributes;
    try {
      this.hostUrl = new URL(attributes.get(P_HOST_URL));
    } catch(MalformedURLException ex) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_HOST_URL, attributes.get(P_HOST_URL)));
    }
    this.userName = attributes.get(P_USER_NAME);
    this.password = attributes.get(P_USER_PASSWORD);
  }

  /**
   * Creates instance of the adaptor.
   */
  public GptAttributesAdaptor() {
    this(new HashMap<>());
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return attributes.entrySet();
  }

  /**
   * Gets host url.
   * @return host url
   */
  public URL getHostUrl() {
    return hostUrl;
  }

  /**
   * Sets host url.
   * @param hostUrl host url 
   */
  public void setHostUrl(URL hostUrl) {
    if (hostUrl==null) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_HOST_URL, hostUrl));
    }
    this.hostUrl = hostUrl;
    attributes.put(P_HOST_URL, hostUrl.toExternalForm());
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
    if (userName!=null) {
      attributes.put(P_USER_NAME, userName);
    } else {
      attributes.remove(P_USER_NAME);
    }
  }

  /**
   * Gets password.
   * @return password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets password.
   * @param password password
   */
  public void setPassword(String password) {
    this.password = password;
    if (password!=null) {
      attributes.put(P_USER_PASSWORD, password);
    } else {
      attributes.remove(P_USER_PASSWORD);
    }
  }
  

  @Override
  public String toString() {
    return String.format("GPT :: url: %s", hostUrl);
  }
}
