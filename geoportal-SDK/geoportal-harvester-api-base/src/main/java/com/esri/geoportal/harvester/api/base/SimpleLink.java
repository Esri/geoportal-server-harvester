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
import com.esri.geoportal.harvester.api.defs.LinkDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.general.Link;
import com.esri.geoportal.harvester.api.general.LinkAction;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple link.
 */
public class SimpleLink implements Link {
  private final LinkAction action;
  private final List<Link> drains;

  /**
   * Creates instance of the link.
   * @param action link action.
   * @param drains drains
   */
  public SimpleLink(LinkAction action, List<Link> drains) {
    this.action = action;
    this.drains = drains;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    action.initialize(context);
    for (Link l: drains) {
      l.initialize(context);
    }
  }

  @Override
  public void terminate() {
    action.terminate();
    for (Link l: drains) {
      l.terminate();
    }
  }

  @Override
  public LinkDefinition getLinkDefinition() {
    LinkDefinition linkDef = new LinkDefinition();
    linkDef.setAction(action.getLinkActionDefinition());
    linkDef.setDrains(drains.stream().map(d->d.getLinkDefinition()).collect(Collectors.toList()));
    return linkDef;
  }

  @Override
  public PublishingStatus push(DataReference dataRef) throws DataProcessorException, DataOutputException {
    PublishingStatus status = action.push(dataRef);
    for (DataReference dr: action.execute(dataRef)) {
      if (drains!=null) {
        for (Link l: drains) {
          status = status.collect(l.push(dr));
        }
      }
    }
    return status;
  }
  
  @Override
  public String toString() {
    return getLinkDefinition().toString();
  }
}
