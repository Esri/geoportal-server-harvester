/*
 * Copyright 2016 Esri, Inc..
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
package com.esri.geoportal.harvester.api.base;

import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Initializable.InitContext;
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.ProcessInstance.Listener;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import java.util.List;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import java.util.stream.Collectors;

/**
 * Data collector.
 */
public class DataCollector {
  private final InputBroker inputBroker;
  private final List<OutputBroker> outputBrokers;
  private final List<Listener> listeners;

  /**
   * Creates instance of the collector.
   * @param source data source
   * @param outputBrokers data destinations
   * @param listeners listeners
   */
  public DataCollector(InputBroker source, List<OutputBroker> outputBrokers, List<Listener> listeners) {
    this.inputBroker = source;
    this.outputBrokers = outputBrokers;
    this.listeners = listeners;
  }
  
  /**
   * Collect available data.
   */
  public void collect() {
    onStart();
    
    try {
      outputBrokers.stream().map(b->new SimpleLink(new BrokerLinkActionAdaptor(b), null)).collect(Collectors.toList());
      Task task = new Task(null, inputBroker, outputBrokers.stream().map(b->new SimpleLink(new BrokerLinkActionAdaptor(b), null)).collect(Collectors.toList()));
      InitContext context = new SimpleInitContext(task,listeners);
      
      inputBroker.initialize(context);
      for (OutputBroker br : outputBrokers) {
        br.initialize(context);
      }
      
      InputBroker.Iterator iterator = inputBroker.iterator(null);
      top: while (iterator.hasNext()) {
        if (Thread.currentThread().isInterrupted()) break;
        DataReference dataReference = iterator.next();
        for (OutputBroker d: outputBrokers) {
          if (Thread.currentThread().isInterrupted()) break top;
          try {
            d.publish(dataReference);
          } catch (DataOutputException ex) {
            onDestinationException(ex);
          }
        }
      }
      
      if (!Thread.currentThread().isInterrupted()) {
        inputBroker.terminate();
        for (OutputBroker br : outputBrokers) {
          br.terminate();
        }
      }
    } catch (DataProcessorException ex) {
      onProcessorException(ex);
    } catch (DataInputException ex) {
      onSourceException(ex);
    } finally {
      onComplete();
    }
  }
  
  protected void onStart() {
    listeners.stream().forEach(l->l.onStatusChange(ProcessInstance.Status.working));
  }
  protected void onComplete() {
    listeners.stream().forEach(l->l.onStatusChange(ProcessInstance.Status.completed));
  }
  protected void onSourceException(DataInputException ex) {
    listeners.stream().forEach(l->l.onError(ex));
  }
  protected void onDestinationException(DataOutputException ex) {
    listeners.stream().forEach(l->l.onError(ex));
  }
  protected void onProcessorException(DataProcessorException ex) {
    listeners.stream().forEach(l->l.onError(ex));
  }
}
