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
package com.esri.geoportal.harvester.engine;

import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultProcessor.
 */
public class DefaultProcessor implements Processor {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultProcessor.class);

  @Override
  public DefaultProcess submit(InputBroker source, List<OutputBroker> destinations) {
    final String harvestDescription = String.format("%s -> [%s}",
            source,
            destinations!=null? destinations.stream().map(d->d.toString()).collect(Collectors.joining(",")): null
    );
    
    LOG.debug(String.format("Initializing default processor for harvesting: %s", harvestDescription));
    
    /*
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        LOG.info(String.format("Started harvest: %s", harvestDescription));
        try {
          if (!destinations.isEmpty()) {
            l.onStarted();
            while(source.hasNext()) {
              if (Thread.currentThread().isInterrupted()) break;
              DataReference dataReference = source.next();
              for (OutputBroker d: destinations) {
                try {
                  d.publish(dataReference);
                  LOG.debug(String.format("Harvested %s during %s", dataReference, harvestDescription));
                  l.onSuccess(dataReference);
                } catch (DataOutputException ex) {
                  LOG.debug(String.format("Failed harvesting %s during %s", dataReference, harvestDescription));
                  l.onError(ex);
                }
              }
            }
          }
        } catch (DataInputException ex) {
          l.onError(ex);
        } finally {
          l.onCompleted();
          LOG.info(String.format("Completed harvest: %s", harvestDescription));
        }
      }
    },"HARVESTING");
    
    thread.setDaemon(true);
    
    return handler;
    */
    
    return null;
  }
}
