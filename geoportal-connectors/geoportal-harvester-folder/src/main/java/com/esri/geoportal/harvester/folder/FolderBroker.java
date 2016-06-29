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

import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import static com.esri.geoportal.harvester.folder.PathUtil.splitPath;
import java.io.OutputStream;
import java.net.URI;

/**
 * Folder broker.
 */
/*package*/ class FolderBroker implements OutputBroker {
  private final FolderConnector connector;
  private final FolderBrokerDefinitionAdaptor definition;

  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition broker definition
   */
  public FolderBroker(FolderConnector connector, FolderBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public OutputConnector getConnector() {
    return connector;
  }

  @Override
  public boolean publish(DataReference ref) throws DataOutputException {
      File f = generateFileName(ref.getSourceUri(), ref.getId());
      boolean created = !f.exists();
      f.getParentFile().mkdirs();
      if (!f.getName().contains(".")) {
        f = f.getParentFile().toPath().resolve(f.getName()+".xml").toFile();
      }
      try (OutputStream output = new FileOutputStream(f);) {
        output.write(ref.getContent());
        return created;
      } catch (Exception ex) {
        throw new DataOutputException(this,"Error publishing data.", ex);
      }
  }

  @Override
  public void close() throws IOException {
    // no need to close
  }
  

  @Override
  public String toString() {
    return String.format("FOLDER [%s]", definition.getRootFolder());
  }
  
  private File generateFileName(URI sourceUri, String id) {
    File rootFolder = definition.getRootFolder().toPath().resolve(sourceUri.getHost()).toFile();
    List<String> subFolder = splitPath(sourceUri.getPath());
    
    File fileName = rootFolder;
    for (String sf : subFolder) {
      fileName = new File(fileName, sf);
    }
    
    return fileName;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }
  
}
