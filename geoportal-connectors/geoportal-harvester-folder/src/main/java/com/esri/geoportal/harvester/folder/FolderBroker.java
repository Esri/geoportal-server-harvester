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

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.constants.MimeTypeUtils;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.BaseProcessInstanceListener;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.util.List;
import java.io.OutputStream;
import java.net.URI;
import static com.esri.geoportal.harvester.folder.PathUtil.splitPath;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Folder broker.
 */
/*package*/ class FolderBroker implements OutputBroker {

  private final static Logger LOG = LoggerFactory.getLogger(FolderBroker.class);
  private final FolderConnector connector;
  private final FolderBrokerDefinitionAdaptor definition;
  private final Set<String> existing = new HashSet<>();
  private volatile boolean preventCleanup;

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition broker definition
   */
  public FolderBroker(FolderConnector connector, FolderBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    try {
      URI ssp = URI.create(context.getTask().getDataSource().getBrokerUri().getSchemeSpecificPart());
      // special case for "jdbc" type schema; strip off everything to rreveal host name
      if ("jdbc".equals(ssp.getScheme())) {
        ssp = URI.create(ssp.getSchemeSpecificPart().replaceAll(";.*$","").replaceAll("^.*?://", "").replaceAll(":.*$", ""));
      }
      String sspRoot = StringUtils.defaultIfEmpty(ssp.getHost(), ssp.getPath());
      Path brokerRootFolder = definition.getRootFolder().toPath().toRealPath().resolve(sspRoot);
      Files.createDirectories(brokerRootFolder);
      if (!context.canCleanup()) {
        preventCleanup = true;
      }
      if (definition.getCleanup() && !preventCleanup) {
        context.addListener(new BaseProcessInstanceListener() {
          @Override
          public void onError(DataException ex) {
            preventCleanup = true;
          }
        });
        fetchExisting(brokerRootFolder);
      }
    } catch (IOException|URISyntaxException ex) {
      throw new DataProcessorException(String.format("Error initializing broker. Exception: "+ex), ex);
    }
  }

  private void fetchExisting(Path folder) throws IOException {
    List<Path> content = Files.list(folder.toRealPath()).collect(Collectors.toList());
    for (Path f: content) {
      if (Thread.currentThread().isInterrupted()) break;
      if (Files.isRegularFile(f)) {
        existing.add(f.toRealPath().toString());
      } else if (Files.isDirectory(f)) {
        fetchExisting(f);
      }
    }
  }

  @Override
  public void terminate() {
    if (definition.getCleanup() && !preventCleanup) {
      for (String f: existing) {
        if (Thread.currentThread().isInterrupted()) break;
        try {
          Files.delete(Paths.get(f));
        } catch (IOException ex) {
          LOG.warn(String.format("Error deleting file: %s", f), ex);
        }
      }
      LOG.info(String.format("%d records has been removed during cleanup.", existing.size()));
    }
  }

  @Override
  public OutputConnector getConnector() {
    return connector;
  }

  @Override
  public PublishingStatus publish(DataReference ref) throws DataOutputException {
    try {
      for (MimeType ct: ref.getContentType()) {
        String extension = MimeTypeUtils.findExtensions(ct).stream().findFirst().orElse(null);
        if (extension!=null) {            
          String title = ref.getTitle();         
          if (title.length() < 1) {
              title = ref.getSourceUri().toString().replace(":", "_").replace("?","_").replace("/","_").replace("\\", "_");             
          }
          Path f = generateFileName(ref.getBrokerUri(), ref.getSourceUri(), title, extension);
          boolean created = !Files.exists(f);
          Files.createDirectories(f.getParent());
          try (OutputStream output = Files.newOutputStream(f)) {
            output.write(ref.getContent(ct));
            existing.remove(f.toRealPath().toString());
            //return created ? PublishingStatus.CREATED : PublishingStatus.UPDATED;
          } catch (Exception ex) {
            if (!Thread.currentThread().isInterrupted()) {
              throw new DataOutputException(this, ref, String.format("Error publishing data: %s Exception: "+ex, ref), ex);
            }
          }
        }
      }
      return PublishingStatus.CREATED;
    } catch (IOException ex) {
      throw new DataOutputException(this, ref, String.format("Error publishing data: %s Exception: "+ex, ref), ex);
    }
  }
  
  private boolean isValidFilename(String filename) {
        String invalidChars = "<>:\"/\\|?*"; // Common invalid characters for Windows
        for (char c : filename.toCharArray()) {
            if (invalidChars.indexOf(c) != -1 || c < 32) { // Check for invalid chars and control characters
                return false;
            }
        }
        return true;
    }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return true;
  }

  @Override
  public String toString() {
    return String.format("FOLDER [%s]", definition.getRootFolder());
  }
  
  private Path generateFileName(URI brokerUri, URI sourceUri, String id, String extension) throws IOException {
    URI ssp = URI.create(brokerUri.getSchemeSpecificPart());
    // special case for "jdbc" type schema; strip off everything to rreveal host name
    if ("jdbc".equals(ssp.getScheme())) {
      ssp = URI.create(ssp.getSchemeSpecificPart().replaceAll(";.*$","").replaceAll("^.*?://", "").replaceAll(":.*$", ""));
    }
    String sspRoot = StringUtils.defaultIfEmpty(ssp.getHost(), ssp.getPath());
    Path brokerRootFolder = definition.getRootFolder().toPath().toRealPath().resolve(sspRoot);

    Path fileName = brokerRootFolder;
    if (sourceUri.getPath() != null) {
      List<String> subFolder = splitPath(sourceUri.getPath().replaceAll("/[a-zA-Z]:/|/$", ""));
      if (!subFolder.isEmpty() && subFolder.get(0).equals(sspRoot)) {
        subFolder.remove(0);
      }     
      for (String sf : subFolder) {
        fileName = Paths.get(fileName.toString(), (id.isBlank() ? sf :id));
      }
      if (!fileName.getFileName().toString().endsWith(extension)) {
        fileName = fileName.getParent().resolve(fileName.getFileName() + "." + extension);
      }
    } else {
      fileName = Paths.get(fileName.toString(), sanitizeFileName(id) + "." + extension);
    }

    return fileName;
  }
  
  private static String sanitizeFileName(String fileName) {
    return fileName.replaceAll("[/\\?%*:|\"<>]", "_");
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

}
