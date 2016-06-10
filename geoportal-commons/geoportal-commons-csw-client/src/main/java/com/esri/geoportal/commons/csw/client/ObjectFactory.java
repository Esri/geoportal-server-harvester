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
package com.esri.geoportal.commons.csw.client;

import com.esri.geoportal.commons.csw.client.impl.Client;
import com.esri.geoportal.commons.csw.client.impl.ProfilesLoader;
import com.esri.geoportal.commons.http.BotsHttpClientFactory;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsMode;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Client factory.
 */
public class ObjectFactory {
  private IProfiles profiles;
  
  /**
   * Gets new instance of the profiles.
   * @return profiles
   */
  public IProfiles newProfiles() {
    if (profiles==null) {
      try {
        ProfilesLoader loader = new ProfilesLoader();
        profiles = loader.load();
      } catch (Exception ex) {
      }
    }
    return profiles;
  }
  
  /**
   * Creates new client.
   * @param baseUrl base URL
   * @param profile profile
   * @param bots robots
   * @param botsMode robots mode
   * @param cred credentials
   * @return instance of the client
   * @throws java.net.MalformedURLException if unable to produce URL from string
   */
  public IClient newClient(String baseUrl, IProfile profile, Bots bots, BotsMode botsMode, SimpleCredentials cred) throws MalformedURLException {
    return new Client(BotsHttpClientFactory.STD.create(bots), new URL(baseUrl), profile, cred);
  }
}
