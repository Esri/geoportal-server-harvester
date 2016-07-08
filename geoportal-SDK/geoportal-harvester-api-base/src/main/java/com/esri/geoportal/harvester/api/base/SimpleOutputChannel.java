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
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataTransformerException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple output channel.
 */
public final class SimpleOutputChannel implements OutputChannel {
  private final OutputBroker outputBroker;
  private final List<LinkProcessor> linkProcessors;

  /**
   * Creates instance of the channel.
   * @param outputBroker output broker
   * @param links links
   */
  public SimpleOutputChannel(OutputBroker outputBroker, ChannelLink...links) {
    this.outputBroker = outputBroker;
    
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
  public ChannelDefinition getChannelDefinition() {
    ChannelDefinition def = new ChannelDefinition();
    def.addAll(linkProcessors.stream().map(p->p.getLinkDefinition()).collect(Collectors.toList()));
    def.add(outputBroker.getEntityDefinition());
    return def;
  }

  @Override
  public OutputBroker.PublishingStatus publish(DataReference ref) throws DataOutputException {
    for (LinkProcessor p: linkProcessors) {
      if (ref==null) break;
      try {
        DataReference result = p.process(ref);
        ref = result!=null? new DataReferenceWrapper(result, ref): null;
      } catch (DataTransformerException ex) {
        throw new DataOutputException(outputBroker, String.format("Error processing chain of links."), ex);
      }
    }
    if (ref==null) return OutputBroker.PublishingStatus.skipped;
    return outputBroker.publish(ref);
  }

  @Override
  public void close() throws Exception {
    outputBroker.close();
    for (LinkProcessor p: linkProcessors) {
      p.close();
    }
  }
}
