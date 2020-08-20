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
package com.esri.geoportal.commons.csw.client.impl;

import com.esri.geoportal.commons.csw.client.IProfiles;

/**
 * Profiles provider.
 */
public class ProfilesProvider {
  private final String cswProfilesFolder;
  private IProfiles profiles;

  public ProfilesProvider(String cswProfilesFolder) {
    this.cswProfilesFolder = cswProfilesFolder;
  }

  public ProfilesProvider() {
    this.cswProfilesFolder = null;
  }
    
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
}
