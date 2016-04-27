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
package com.esri.geoportal.harvester.csw;

import com.esri.geoportal.commons.csw.client.IProfile;
import com.esri.geoportal.commons.csw.client.IProfiles;
import com.esri.geoportal.commons.csw.client.ObjectFactory;
import com.esri.geoportal.harvester.api.support.BotsAttributesAdaptor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * CSW attributes adaptor.
 */
public class CswAttributesAdaptor extends BotsAttributesAdaptor {

  private static final String P_HOST_URL = "csw.host.url";
  private static final String P_PROFILE_ID = "csw.profile.id";
  
  private URL hostUrl;
  private IProfile profile;

  /**
   * Creates instance of the adaptor.
   * @param attributes attributes
   */
  public CswAttributesAdaptor(Map<String, String> attributes) {
    super(attributes);
    try {
      this.hostUrl = new URL(attributes.get(P_HOST_URL));
    } catch(MalformedURLException ex) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_HOST_URL, attributes.get(P_HOST_URL)));
    }
    ObjectFactory of = new ObjectFactory();
    IProfiles profiles = of.newProfiles();
    IProfile p = profiles.getProfileById(attributes.get(P_PROFILE_ID));
    if (p==null) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_PROFILE_ID, attributes.get(P_PROFILE_ID)));
    }
    this.profile = p;
  }
  
  /**
   * Creates empty adaptor.
   */
  public CswAttributesAdaptor() {
    super(new HashMap<>());
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
   * Gets host url.
   * @return host url
   */
  public URL getHostUrl() {
    return hostUrl;
  }
  
  /**
   * Gets profile.
   * @return profile
   */
  public IProfile getProfile() {
    return profile;
  }
  
  /**
   * Sets profile.
   * @param profile profile
   */
  public void setProfile(IProfile profile) {
    if (profile==null) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_PROFILE_ID, profile));
    }
    this.profile = profile;
    attributes.put(P_PROFILE_ID, profile.getId());
  }
  
  @Override
  public String toString() {
    return String.format("CSW :: url: %s, profile: %s", hostUrl, profile);
  }
}
