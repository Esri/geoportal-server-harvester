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
package com.esri.geoportal.harvester.folder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Folder attributes adaptor.
 */
public class FolderAttributesAdaptor extends AbstractMap<String,String> {
  private static final String P_ROOT_FOLDER = "folder.root.folder";
  private static final String P_HOST_URL    = "folder.host.url";
  
  private final Map<String,String> attributes;
  
  private File rootFolder;
  private URL hostUrl;

  /**
   * Creates instance of the attributes.
   * @param attributes attributes
   */
  public FolderAttributesAdaptor(Map<String, String> attributes) {
    this.attributes = attributes;
    try {
      this.rootFolder = new File(attributes.get(P_ROOT_FOLDER));
    } catch (NullPointerException ex) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_ROOT_FOLDER, attributes.get(P_ROOT_FOLDER)));
    }
    try {
      this.hostUrl = new URL(attributes.get(P_HOST_URL));
    } catch(MalformedURLException ex) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_HOST_URL, attributes.get(P_HOST_URL)));
    }
  }
  
  public FolderAttributesAdaptor() {
    this(new HashMap<>());
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return attributes.entrySet();
  }

  /**
   * Gets root folder.
   * @return root folder
   */
  public File getRootFolder() {
    return rootFolder;
  }

  /**
   * Sets root folder.
   * @param rootFolder root folder
   */
  public void setRootFolder(File rootFolder) {
    if (rootFolder==null) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_ROOT_FOLDER, rootFolder));
    }
    this.rootFolder = rootFolder;
    attributes.put(P_ROOT_FOLDER, rootFolder.toString());
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
  
  @Override
  public String toString() {
    return String.format("FOLDER :: root: %s, host: %s", rootFolder, hostUrl);
  }
}
