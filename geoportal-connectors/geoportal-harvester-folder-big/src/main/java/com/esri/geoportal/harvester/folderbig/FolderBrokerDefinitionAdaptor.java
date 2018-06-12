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
package com.esri.geoportal.harvester.folderbig;

import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Map;

import static com.esri.geoportal.harvester.folderbig.FolderConstants.*;

/**
 * Folder broker definition adaptor.
 */
/*package*/ class FolderBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {
  
  private File rootFolder;
  private boolean cleanup;
  private boolean splitFolders;
  private Integer splitSize = 200;

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
      try {
        this.splitSize = Integer.parseInt(get(P_FOLDER_SPLIT_SIZE));
      } catch( Exception ex){
        throw new InvalidDefinitionException(String.format("Invalid %s: %s",P_FOLDER_SPLIT_SIZE,get(P_FOLDER_SPLIT_SIZE)), ex);
      }
      cleanup  = Boolean.parseBoolean(get(P_FOLDER_CLEANUP));
      splitFolders = Boolean.parseBoolean(get(P_FOLDER_SPLIT_FOLDERS));
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,P_ROOT_FOLDER);
    consume(params,P_FOLDER_CLEANUP);
    consume(params,P_FOLDER_SPLIT_SIZE);
    consume(params,P_FOLDER_SPLIT_FOLDERS);
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

  /**
   * Gets the maximum folder size
   * @return splitSize
   */
  public Integer getSplitSize() {
    return splitSize;
  }

  public void setSplitSize(Integer splitSize) {
    this.splitSize = splitSize;
  }

  /**
   * Gets if the collection should be split into folders of SplitSize
   * @return splitFolders <code>true</code>  if folders will be split
   */
  public boolean getSplitFolders() {
    return splitFolders;
  }

  /**
   * Sets if the collection should be split into folders of SplitSize
   * @param splitFolders <code>true</code> to split folders
   */
  public void setSplitFolders(boolean splitFolders) {
    this.splitFolders = splitFolders;
  }
}
