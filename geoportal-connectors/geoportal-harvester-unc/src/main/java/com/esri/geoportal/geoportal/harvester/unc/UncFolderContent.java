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
package com.esri.geoportal.geoportal.harvester.unc;

import java.util.List;

/**
 * UNC folder content.
 */
/*package*/ class UncFolderContent {
  private final UncFolder rootFolder;
  private final List<UncFolder> subFolders;
  private final List<UncFile> files;

  /**
   * Creates instance of the UNC folder content.
   * @param rootFolder root folder
   * @param subFolders list of sub folders
   * @param files list of files
   */
  public UncFolderContent(UncFolder rootFolder, List<UncFolder> subFolders, List<UncFile> files) {
    this.rootFolder = rootFolder;
    this.subFolders = subFolders;
    this.files = files;
  }

  /**
   * Gets root folder.
   * @return root folder
   */
  public UncFolder getRootFolder() {
    return rootFolder;
  }

  /**
   * Gets sub folders.
   * @return sub folders
   */
  public List<UncFolder> getSubFolders() {
    return subFolders;
  }

  /**
   * Gets files.
   * @return files
   */
  public List<UncFile> getFiles() {
    return files;
  }
}
