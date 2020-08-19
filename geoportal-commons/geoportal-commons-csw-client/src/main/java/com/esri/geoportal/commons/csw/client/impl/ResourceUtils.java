/*
 * Copyright 2020 Esri, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copyResources of the License at
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Resource folder copier.
 */
public class ResourceUtils {
  private static final Logger LOG = Logger.getLogger(ResourceUtils.class.getName());
  
  public static void copyResources(String resourceFolder, File destinationFolder) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    URL url = loader.getResource(resourceFolder);
    String path = url.getPath();
    File[] files = new File(path).listFiles();

    if (files != null) {
      destinationFolder.mkdirs();
      
      Arrays.stream(files).forEach(f -> {
        try {
          FileUtils.copyToDirectory(f, destinationFolder);
        } catch (IOException ex) {
          LOG.log(Level.FINE, String.format("Error copying %s to %s", f, destinationFolder), ex);
        }
      });
    }
  }
}
