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

import com.esri.geoportal.commons.csw.client.IProfile;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Profiles test.
 */
public class ProfilesTest {
  

  @Test
  public void testProfiles() throws Exception {
    Profiles profiles = new ProfilesLoader().load();
    
    IProfile defaultProfile = profiles.getDefaultProfile();
    
    assertNotNull("No default profile", defaultProfile);
    
    List<IProfile> allProfiles = profiles.listAll();
    
    assertNotNull("No profiles", allProfiles);
    assertNotEquals("No profiles",allProfiles.size(), 0);
    
    IProfile profile = profiles.getProfileById(defaultProfile.getId());
    
    assertNotNull("No profile by id", profile);
  }
  
}
