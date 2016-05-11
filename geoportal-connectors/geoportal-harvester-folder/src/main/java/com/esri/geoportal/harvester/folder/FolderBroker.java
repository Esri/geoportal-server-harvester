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

import com.esri.geoportal.harvester.api.Connector;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import static com.esri.geoportal.harvester.folder.PathUtil.sanitizeFileName;
import static com.esri.geoportal.harvester.folder.StringListUtil.head;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import static com.esri.geoportal.harvester.folder.PathUtil.splitPath;
import java.io.OutputStream;

/**
 * Folder broker.
 */
public class FolderBroker implements OutputBroker {
  private final FolderConnector connector;
  private final FolderBrokerDefinitionAdaptor definition;
  private final File rootFolder;
  private final List<String> subFolder;

  public FolderBroker(FolderConnector connector, FolderBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
    this.rootFolder = definition.getRootFolder().toPath().resolve(definition.getHostUrl().getHost()).toFile();
    this.subFolder = splitPath(definition.getHostUrl().getPath());
  }

  @Override
  public Connector getConnector() {
    return connector;
  }

  @Override
  public void publish(DataReference ref) throws DataOutputException {
      File f = generateFileName(ref.getSourceUri().toASCIIString());
      f.getParentFile().mkdirs();
      if (!f.getName().contains(".")) {
        f = f.getParentFile().toPath().resolve(f.getName()+".xml").toFile();
      }
      try (OutputStream output = new FileOutputStream(f);) {
        output.write(ref.getContent());
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
  
  private File generateFileName(String uri) {
    String sUri = uri;
    
    File fileName = rootFolder;
    try {
      List<String> stock;
      
      URL u = new URL(sUri);
      List<String> path = splitPath(u);
      if (path.size()>0) {
        if (path.size()>1) {
          stock = StringListUtil.merge(subFolder,head(path, path.size()-1));
          stock.add(path.get(path.size()-1));
        } else {
          stock = Arrays.asList(new String[0]);
          stock.addAll(subFolder);
          stock.addAll(path);
        }
      } else {
        stock = subFolder;
      }
      
      for (String t: stock) {
        fileName = fileName.toPath().resolve(t).toFile();
      }
      return fileName;
    } catch (MalformedURLException ex) {
      if (UuidUtil.isUuid(sUri)) {
        fileName = fileName.toPath().resolve(sanitizeFileName(sUri)+".xml").toFile();
        return fileName;
      } else {
        File f = new File(sUri);
        for (String t: StringListUtil.merge(subFolder,splitPath(f))) {
          fileName = fileName.toPath().resolve(t).toFile();
        }
        return fileName;
      }
    }
  }
  
}
