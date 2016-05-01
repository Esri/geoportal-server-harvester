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
package com.esri.geoportal.harvester.csw;

import com.esri.geoportal.commons.csw.client.IProfile;
import com.esri.geoportal.commons.csw.client.IProfiles;
import com.esri.geoportal.commons.csw.client.ObjectFactory;
import com.esri.geoportal.harvester.api.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.support.BotsAttributesAdaptor;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * CSW definition.
 */
public class CswDefinition extends BotsAttributesAdaptor  {
  public static final String TYPE = "CSW";
  
  public static final String P_HOST_URL = "csw.host.url";
  public static final String P_PROFILE_ID = "csw.profile.id";
  
  private URL hostUrl;
  private IProfile profile;

  @Override
  public String getType() {
    return TYPE;
  }

  
  public CswDefinition validate() throws InvalidDefinitionException {
    if (getHostUrl()==null) {
      throw new InvalidDefinitionException(String.format("Invalid host url: %s", this.get(P_HOST_URL)));
    }
    if (getProfile()==null) {
      throw new InvalidDefinitionException(String.format("Invalid profile: %s", this.get(P_PROFILE_ID)));
    }
    return this;
  }
  
  public URL getHostUrl() {
    if (hostUrl==null) {
      try {
        hostUrl = new URL(this.get(P_HOST_URL));
      } catch (MalformedURLException|NullPointerException ex) {
      }
    }
    return hostUrl;
  }
  
  public void setHostUrl(URL url) {
    this.hostUrl = url;
    this.put(P_HOST_URL, url.toExternalForm());
  }
  
  /**
   * Gets profile.
   * @return profile
   */
  public IProfile getProfile() {
    if (profile==null) {
      ObjectFactory of = new ObjectFactory();
      IProfiles profiles = of.newProfiles();
      profile = profiles.getProfileById(this.get(P_PROFILE_ID));
    }
    return profile;
  }
  
  /**
   * Sets profile.
   * @param profile profile
   */
  public void setProfile(IProfile profile) {
    this.profile = profile;
    this.put(P_PROFILE_ID, profile.getId());
  }

}
