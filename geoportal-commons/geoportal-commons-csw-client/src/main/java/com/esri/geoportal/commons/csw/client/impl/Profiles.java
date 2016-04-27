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
package com.esri.geoportal.commons.csw.client.impl;

import com.esri.geoportal.commons.csw.client.IProfile;
import com.esri.geoportal.commons.csw.client.IProfiles;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * Profiles implementation.
 */
public class Profiles implements IProfiles {
  private static final String DEFAULT_PROFILE_ID = "urn:ogc:CSW:2.0.2:HTTP:OGCCORE:ESRI:GPT";
  private static Profiles instance;
  
  private List<IProfile> orderedList = new ArrayList<>();
  private Map<String,IProfile> mappedValued = new HashMap<>();

  /**
   * Adds a profile.
   * @param value profile
   */
  public void add(IProfile value) {
    orderedList.add(value);
    mappedValued.put(value.getId(), value);
  }

  @Override
  public IProfile getProfileById(String id) {
    return mappedValued.get(id);
  }
  
  @Override
  public List<IProfile> listAll() {
    return orderedList;
  }

  @Override
  public IProfile getDefaultProfile() {
    IProfile defaultProfile = getProfileById(DEFAULT_PROFILE_ID);
    if (defaultProfile==null && !orderedList.isEmpty()) {
      defaultProfile = orderedList.get(0);
    }
    return defaultProfile;
  }
  
  /**
   * Gets instance of the profiles.
   * <p>
   * Loads configuration if called first time.
   * @return profiles
   * @throws IOException if loading profiles from configuration fails
   * @throws ParserConfigurationException if unable to obtain XML parser
   * @throws SAXException if unable to parse XML document
   * @throws XPathExpressionException if invalid XPath expression
   */
  public static Profiles getInstance() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
    if (instance==null) {
      ProfilesLoader loader = new ProfilesLoader();
      instance = loader.load();
    }
    return instance;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    orderedList.forEach(k->sb.append(sb.length()>0?",":"").append(k.getId()));
    return String.format("[%s]", sb);
  }
}
