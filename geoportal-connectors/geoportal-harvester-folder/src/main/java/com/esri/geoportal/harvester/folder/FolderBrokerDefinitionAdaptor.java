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

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.io.File;
import org.apache.commons.lang3.StringUtils;

/**
 * Folder broker definition adaptor.
 */
/*package*/ class FolderBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {

  public static final String P_ROOT_FOLDER = "folder-root-folder";
  public static final String P_FOLDER_CLEANUP = "folder-cleanup";
  
  private File rootFolder;
  private boolean cleanup;

  /**
   * Creates instance of the adaptor.
   * @param entityDefinition broker definition
   * @throws InvalidDefinitionException if invalid definition
   */
  public FolderBrokerDefinitionAdaptor(EntityDefinition entityDefinition) throws InvalidDefinitionException {
    super(entityDefinition);
    if (StringUtils.trimToEmpty(entityDefinition.getType()).isEmpty()) {
      entityDefinition.setType(FolderConnector.TYPE);
    } else if (!FolderConnector.TYPE.equals(entityDefinition.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      try {
        this.rootFolder = new File(get(P_ROOT_FOLDER));
      } catch (NullPointerException ex) {
        throw new InvalidDefinitionException(String.format("Invalid %s: %s",P_ROOT_FOLDER,get(P_ROOT_FOLDER)), ex);
      }
      cleanup  = Boolean.parseBoolean(get(P_FOLDER_CLEANUP));
    }
  }

  /**
   * Gets root folder.
   *
   * @return root folder
   */
  public File getRootFolder() {
    return rootFolder;
  }

  /**
   * Sets root folder.
   *
   * @param rootFolder root folder
   */
  public void setRootFolder(File rootFolder) {
    this.rootFolder = rootFolder;
    set(P_ROOT_FOLDER, rootFolder.toString());
  }

  /**
   * Gets permission to cleanup.
   * @return <code>true</code> if cleanup permitted
   */
  public boolean getCleanup() {
    return cleanup;
  }

  /**
   * Sets permission to cleanup.
   * @param cleanup <code>true</code> to permit cleanup
   */
  public void setCleanup(boolean cleanup) {
    this.cleanup = cleanup;
    set(P_FOLDER_CLEANUP, Boolean.toString(cleanup));
  }
}
