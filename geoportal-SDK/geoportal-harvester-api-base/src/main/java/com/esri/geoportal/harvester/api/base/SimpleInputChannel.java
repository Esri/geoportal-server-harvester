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
package com.esri.geoportal.harvester.api.base;

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Filter;
import com.esri.geoportal.harvester.api.FilterInstance;
import com.esri.geoportal.harvester.api.Transformer;
import com.esri.geoportal.harvester.api.TransformerInstance;
import com.esri.geoportal.harvester.api.defs.ChannelDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataTransformerException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple input channel.
 */
public final class SimpleInputChannel implements InputChannel {
  
  private final InputBroker inputBroker;
  private final List<LinkProcessor> linkProcessors;
  
  private DataReference next;

  /**
   * Creates instance of the channel.
   * @param inputBroker input broker
   * @param links links
   */
  public SimpleInputChannel(InputBroker inputBroker, ChannelLink...links) {
    this.inputBroker = inputBroker;
    
    this.linkProcessors = Arrays.asList(links).stream()
            .map(link->{
              if (link instanceof Filter) {
                return new FilterProcessor((FilterInstance) link);
              } else if (link instanceof Transformer) {
                return new TransformerProcessor((TransformerInstance) link);
              } else {
                return null;
              }
            })
            .filter(instance->instance!=null)
            .collect(Collectors.toList());
  }

  @Override
  public boolean hasNext() throws DataInputException {
    if (next==null) {
      if (inputBroker.hasNext()) {
        next = inputBroker.next();
        for (LinkProcessor p: linkProcessors) {
          if (next==null) break;
          try {
            DataReference result = p.process(next);
            next = result!=null? new DataReferenceWrapper(result, next): null;
          } catch (DataTransformerException ex) {
            throw new DataInputException(inputBroker, String.format("Error processing chain of links."), ex);
          }
        }
        if (next==null) {
          return hasNext();
        }
      }
    }
    return next!=null;
  }

  @Override
  public ChannelDefinition getChannelDefinition() {
    ChannelDefinition def = new ChannelDefinition();
    def.add(inputBroker.getEntityDefinition());
    def.addAll(linkProcessors.stream().map(p->p.getLinkDefinition()).collect(Collectors.toList()));
    return def;
  }

  @Override
  public DataReference next() throws DataInputException {
    if (next==null) {
      throw new DataInputException(inputBroker, String.format("Error getting next data reference."));
    }
    DataReference ref = next;
    next = null;
    return ref;
  }

  @Override
  public void close() throws Exception {
    inputBroker.close();
    for (LinkProcessor p: linkProcessors) {
      p.close();
    }
    next = null;
  }
  
  @Override
  public String toString() {
    ArrayList<String> parts = new ArrayList<>();
    parts.add(inputBroker.toString());
    parts.addAll(linkProcessors.stream().map(LinkProcessor::toString).collect(Collectors.toList()));
    return parts.toString();
  }
}
