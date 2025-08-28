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
package com.esri.geoportal.harvester.unc;

import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * UNC broker.
 */
/*package*/ class UncBroker implements InputBroker {
  private final UncConnector connector;
  private final UncBrokerDefinitionAdaptor definition;

  private LinkedList<UncFolder> subFolders;
  private LinkedList<UncFile> files;
  
  TaskDefinition td;
  
  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   */
  public UncBroker(UncConnector connector, UncBrokerDefinitionAdaptor definition) {
    this.connector = connector;
    this.definition = definition;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    td = context.getTask().getTaskDefinition();
  }

  @Override
  public void terminate() {
    // nothing to terminate
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  @Override
  public InputConnector getConnector() {
    return connector;
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    String path = definition.getRootFolder().toURI().getPath().replaceAll("/[a-zA-Z]:/|/$", "");
    return new URI("UNC",path, null);
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    return new UncIterator(iteratorContext);
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return true;
  }

  @Override
  public String toString() {
    return String.format("UNC [%s]", definition.getRootFolder());
  }

  @Override
  public DataContent readContent(String id) throws DataInputException {
    try {
      // Restrict file access to within the configured root folder
      java.io.File rootFolderFile = definition.getRootFolder();
      java.nio.file.Path rootFolder = rootFolderFile.toPath().toAbsolutePath().normalize();
      java.nio.file.Path requestedPath = rootFolder.resolve(id).normalize();
      if (!requestedPath.startsWith(rootFolder)) {
        throw new DataInputException(this, String.format("Invalid file path: %s", id), null);
      }
      UncFile file = new UncFile(this, requestedPath);;
      return file.readContent();
    } catch (IOException|URISyntaxException ex) {
      throw new DataInputException(this, String.format("Error reading content: %s : Exception: "+ex, id), ex);
    }
  }

  /**
   * UNC iterator.
   */
  private class UncIterator implements InputBroker.Iterator {
    private final IteratorContext iteratorContext;

    /**
     * Creates instance of the iterator.
     * @param iteratorContext iterator context
     */
    public UncIterator(IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }
    
    
    @Override
    public boolean hasNext() throws DataInputException {

      try {
        if (files!=null && !files.isEmpty()) {
          return true;
        }

        if (subFolders!=null && !subFolders.isEmpty()) {
          UncFolder subFolder = subFolders.poll();
          UncFolderContent content = subFolder.readContent();
          content.getSubFolders().forEach(f->subFolders.offer(f));
          files = new LinkedList<>(content.getFiles());
          return hasNext();
        }

        if (subFolders==null) {
          UncFolderContent content = new UncFolder(UncBroker.this, Paths.get(definition.getRootFolder().getAbsolutePath()), definition.getPattern(), iteratorContext.getLastHarvestDate()).readContent();
          subFolders = new LinkedList<>(content.getSubFolders());
          files = new LinkedList<>(content.getFiles());
          return hasNext();
        }

        return false;
      } catch (IOException|URISyntaxException ex) {
        throw new DataInputException(UncBroker.this, "Error reading data. : Exception: "+ex, ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      try {
        UncFile file = files.poll();
        return file.readContent();
      } catch (IOException|URISyntaxException ex) {
        throw new DataInputException(UncBroker.this, "Error reading data. : Exception: "+ex, ex);
      }
    }
  }
  
}
