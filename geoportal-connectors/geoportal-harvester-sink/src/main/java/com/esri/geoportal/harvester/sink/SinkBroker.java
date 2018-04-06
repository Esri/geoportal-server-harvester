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
package com.esri.geoportal.harvester.sink;

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

/**
 * Sink broker.
 */
/*package*/ class SinkBroker implements InputBroker {
  private final SinkConnector connector;
  private final SinkBrokerDefinitionAdaptor definition;

  private LinkedList<SinkFile> files;
  
  TaskDefinition td;
  
  /**
   * Creates instance of the broker.
   * @param connector connector
   * @param definition definition
   */
  public SinkBroker(SinkConnector connector, SinkBrokerDefinitionAdaptor definition) {
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
    return new URI("SINK",path, null);
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    return new SinkIterator(iteratorContext);
  }

  @Override
  public String toString() {
    return String.format("UNC [%s]", definition.getRootFolder());
  }

  /**
   * Sink iterator.
   */
  private class SinkIterator implements InputBroker.Iterator {
    private final IteratorContext iteratorContext;

    /**
     * Creates instance of the iterator.
     * @param iteratorContext iterator context
     */
    public SinkIterator(IteratorContext iteratorContext) {
      this.iteratorContext = iteratorContext;
    }
    
    
    @Override
    public boolean hasNext() throws DataInputException {
      return false;
    }

    @Override
    public DataReference next() throws DataInputException {
      throw new DataInputException(SinkBroker.this, "Error reading data.");
    }
  }
  
}
