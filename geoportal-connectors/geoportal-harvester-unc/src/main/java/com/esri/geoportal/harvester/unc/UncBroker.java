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

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Map;

/**
 * UNC broker.
 */
/*package*/ class UncBroker implements InputBroker {
  private final UncConnector connector;
  private final UncBrokerDefinitionAdaptor definition;

  private LinkedList<UncFolder> subFolders;
  private LinkedList<UncFile> files;
  
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
    // nothing to initialize
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
  public Iterator iterator(Map<String,Object> attributes) throws DataInputException {
    return new UncIterator();
  }

  @Override
  public String toString() {
    return String.format("UNC [%s]", definition.getRootFolder());
  }

  /**
   * UNC iterator.
   */
  private class UncIterator implements InputBroker.Iterator {
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
          UncFolderContent content = new UncFolder(UncBroker.this, definition.getRootFolder(), definition.getPattern()).readContent();
          subFolders = new LinkedList<>(content.getSubFolders());
          files = new LinkedList<>(content.getFiles());
          return hasNext();
        }

        return false;
      } catch (IOException|URISyntaxException ex) {
        throw new DataInputException(UncBroker.this, "Error reading data.", ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      try {
        UncFile file = files.poll();
        return file.readContent();
      } catch (IOException|URISyntaxException ex) {
        throw new DataInputException(UncBroker.this, "Error reading data.", ex);
      }
    }
  }
  
}
