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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sink broker.
 */
/*package*/ class SinkBroker implements InputBroker {
  private static final Logger LOG = LoggerFactory.getLogger(SinkBroker.class);
  private final SinkConnector connector;
  private final SinkBrokerDefinitionAdaptor definition;

  private final LinkedList<SinkFile> files = new LinkedList<>();
  
  TaskDefinition td;
  private Path dropPath;
  private WatchService watchService;
  private Thread watchThread;
  private final Object lock = new Object();
  
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
    context.preventCleanup();
    definition.override(context.getParams());
    td = context.getTask().getTaskDefinition();
    dropPath = Paths.get(definition.getRootFolder().getAbsolutePath());
    try {
      watchService = FileSystems.getDefault().newWatchService();
      dropPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
      watchThread = new Thread(() -> {
        WatchKey watchKey;
        try {
          do {
            watchKey = watchService.take();
            watchKey.pollEvents();
            
            synchronized(lock) {
              lock.notifyAll();
            }

            watchKey.reset();
          } while (!Thread.interrupted());
        } catch (InterruptedException|IllegalMonitorStateException ex) {
          // ignore
        }
      }, String.format("Folder watching thread on %s", dropPath.toString()));
      watchThread.start();
    } catch (IOException ex) {
      throw new DataProcessorException(String.format("Error creating folder watch service."), ex);
    }
  }

  @Override
  public void terminate() {
    if (watchService != null) {
      try {
        watchService.close();
      } catch (IOException ex) {
        LOG.warn(String.format("Error terminating broker: %s", definition.toString()), ex);
      }
    }
    
    if (watchThread != null) {
      watchThread.interrupt();
    }
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

  @Override
  public DataContent readContent(String id) throws DataInputException {
    return null;
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return true;
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
    
    
    private List<SinkFile> listFiles() throws IOException {
      Pattern pattern = Pattern.compile(".*\\.xml$", Pattern.CASE_INSENSITIVE);
      return Files.list(dropPath).filter(path -> pattern.matcher(path.toString()).matches()).map(path -> new SinkFile(connector.getCtx(), SinkBroker.this, path)).collect(Collectors.toList());
    }
    
    @Override
    public boolean hasNext() throws DataInputException {
      try {
        if (files!=null && !files.isEmpty()) {
          return true;
        }
        
        files.addAll(listFiles());
        
        if (!files.isEmpty()) {
          return true;
        }
        
        synchronized(lock) {
          lock.wait();
        }
        
        return hasNext();
      } catch (InterruptedException ex) {
        return false;
      } catch (IOException ex) {
        throw new DataInputException(SinkBroker.this, "Error reading data.", ex);
      }
    }

    @Override
    public DataReference next() throws DataInputException {
      try {
        SinkFile file = files.poll();
        return file.readContent();
      } catch (IOException|URISyntaxException ex) {
        throw new DataInputException(SinkBroker.this, "Error reading data.", ex);
      }
    }
  }
  
}
