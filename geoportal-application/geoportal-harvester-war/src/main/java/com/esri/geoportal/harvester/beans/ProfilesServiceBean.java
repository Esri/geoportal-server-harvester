/*
 * Copyright 2020 Esri, Inc.
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
package com.esri.geoportal.harvester.beans;

import com.esri.geoportal.commons.csw.client.impl.ProfilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;

/**
 * Profiles service bean.
 */
public class ProfilesServiceBean extends ProfilesService {
  private static final Logger LOG = LoggerFactory.getLogger(ProfilesServiceBean.class);
  
  public ProfilesServiceBean(String cswProfilesFolder) {
    super(cswProfilesFolder);
  }
  
  public void init() throws BeanInitializationException {
    try {
      initialize();
    } catch (Exception ex) {
      throw new BeanInitializationException(String.format("Error loading CSW profiles"), ex);
    }
    LOG.info("ProfilesServiceBean initialized.");
  }
}
