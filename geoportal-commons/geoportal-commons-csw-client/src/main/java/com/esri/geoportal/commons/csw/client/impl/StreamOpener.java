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
package com.esri.geoportal.commons.csw.client.impl;

import static com.esri.geoportal.commons.csw.client.impl.Constants.CONFIG_FOLDER_PATH;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Stream opener.
 */
public interface StreamOpener {
  InputStream open(String fileName) throws IOException;
  
  class ResourceOpener implements StreamOpener {

    @Override
    public InputStream open(String fileName) throws IOException {
      return Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FOLDER_PATH + "/" + fileName.replaceAll("^/+", ""));
    }
    
    public String toString() {
      return "boundled resource";
    }
    
  }
  
  class FolderOpener implements StreamOpener {
    private final File root;
    
    public FolderOpener(String root) {
      this.root = new File(ResourceUtils.resolveDestinationFolder(root));
    }

    @Override
    public InputStream open(String fileName) throws IOException {
      return new FileInputStream(new File(root, fileName));
    }
    
    public String toString() {
      return root.getAbsolutePath();
    }
  } 
}
