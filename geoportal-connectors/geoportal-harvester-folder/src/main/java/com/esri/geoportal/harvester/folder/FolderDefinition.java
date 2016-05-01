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
package com.esri.geoportal.harvester.folder;

import com.esri.geoportal.harvester.api.BrokerDefinition;
import com.esri.geoportal.harvester.api.InvalidDefinitionException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Folder definition.
 */
public class FolderDefinition extends BrokerDefinition {

  public static final String TYPE = "FOLDER";

  public static final String P_ROOT_FOLDER = "folder.root.folder";
  public static final String P_HOST_URL = "folder.host.url";

  private File rootFolder;
  private URL hostUrl;

  @Override
  public String getType() {
    return TYPE;
  }

  public FolderDefinition validate() throws InvalidDefinitionException {
    if (getRootFolder() == null) {
      throw new InvalidDefinitionException(String.format("Invalid root folder: %s", this.get(P_ROOT_FOLDER)));
    }
    if (getHostUrl() == null) {
      throw new InvalidDefinitionException(String.format("Invalid host url: %s", this.get(P_HOST_URL)));
    }
    return this;
  }

  /**
   * Gets root folder.
   *
   * @return root folder
   */
  public File getRootFolder() {
    if (rootFolder == null) {
      try {
        this.rootFolder = new File(this.get(P_ROOT_FOLDER));
      } catch (NullPointerException ex) {
      }
    }
    return rootFolder;
  }

  /**
   * Sets root folder.
   *
   * @param rootFolder root folder
   */
  public void setRootFolder(File rootFolder) {
    this.rootFolder = rootFolder;
    this.put(P_ROOT_FOLDER, rootFolder.toString());
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
}
