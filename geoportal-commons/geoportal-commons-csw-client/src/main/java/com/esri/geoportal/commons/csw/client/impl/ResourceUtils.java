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
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;

/**
 * Resource folder copier.
 */
public class ResourceUtils {
  private static final Logger LOG = Logger.getLogger(ResourceUtils.class.getName());
  
  public static void copyResources(String resourceFolder, String destinationFolder) throws IOException {
    File folder = destinationFolder.startsWith("~")?
      new File(System.getProperty("user.home"), destinationFolder.substring(1)):
      new File(destinationFolder);
    copyResources(resourceFolder, folder);
  }
  
  public static void copyResources(String resourceFolder, File destinationFolder) throws IOException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    URL url = loader.getResource(resourceFolder);
    String path = url.getPath();
    
    ZipFile zip = new ZipFile(path.split("!")[0].replaceAll("^file:", ""));
    List<FileCopier> fileCopiers = Collections.list(zip.entries()).stream()
      .filter(ze -> !ze.isDirectory())
      .filter(ze -> ze.getName().startsWith(resourceFolder))
      .map(ze -> {
        File inputFile = new File(ze.getName().substring(resourceFolder.length()));
        File parentFile = inputFile.getParentFile();
        File destPath = parentFile!=null? new File(destinationFolder, parentFile.getPath()): destinationFolder;
        File destFile = new File(destPath, inputFile.getName());
        return new FileCopier(ze, destFile);
      }).collect(Collectors.toList());
    
    if (!fileCopiers.isEmpty()) {
      destinationFolder.mkdirs();
      for (FileCopier fc: fileCopiers) {
        fc.doCopy(zip);
      }
    }
  }
  
  private static class FileCopier {
    private final ZipEntry entry;
    private final File toDir;

    public FileCopier(ZipEntry entry, File toDir) {
      this.entry = entry;
      this.toDir = toDir;
    }
    
    public void doCopy(ZipFile zipFile) throws IOException {
      try (InputStream input = zipFile.getInputStream(entry)) {
        toDir.getParentFile().mkdirs();
        FileUtils.copyToFile(input, toDir);
      }
    }
    
  }
}
