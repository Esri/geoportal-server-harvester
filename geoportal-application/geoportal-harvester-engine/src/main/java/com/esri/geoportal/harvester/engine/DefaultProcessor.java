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

/**
 * DefaultProcessor.
 */
public class DefaultProcessor implements Processor<String> {

  @Override
  public Handler initialize(InputBroker<String> source, List<OutputBroker<String>> destinations, Listener l) {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          if (!destinations.isEmpty()) {
            l.onStarted();
            while(source.hasNext()) {
              if (Thread.currentThread().isInterrupted()) break;
              DataReference<String> dataReference = source.next();
              for (OutputBroker<String> d: destinations) {
                try {
                  d.publish(dataReference);
                  l.onSuccess(dataReference);
                } catch (DataOutputException ex) {
                  l.onError(ex);
                }
              }
            }
          }
        } catch (DataInputException ex) {
          l.onError(ex);
        } finally {
          l.onCompleted();
        }
      }
    },"HARVESTING");
    
    thread.setDaemon(true);
    
    Handler handler = new Handler() {
      @Override
      public void begin() {
        thread.start();
      }

      @Override
      public void abort() {
        thread.interrupt();
      }

      @Override
      public boolean isActive() {
        return thread.isAlive();
      }
    };
    
    return handler;
  }
}
