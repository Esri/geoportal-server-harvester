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
import com.esri.geoportal.harvester.api.FilterInstance;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.general.LinkAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Filter link action adaptor.
 */
public final class FilterLinkActionAdaptor implements LinkAction {
  private final FilterInstance filter;

  /**
   * Creates instance of the adaptor.
   * @param filter filter.
   */
  public FilterLinkActionAdaptor(FilterInstance filter) {
    this.filter = filter;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    filter.initialize(context);
  }

  @Override
  public void terminate() {
    filter.terminate();
  }

  @Override
  public EntityDefinition getLinkActionDefinition() {
    return filter.getFilterDefinition();
  }

  @Override
  public List<DataReference> execute(DataReference dataRef) throws DataOutputException {
    return filter.test(dataRef)? Arrays.asList(new DataReference[]{dataRef}): Collections.emptyList();
  }

  @Override
  public PublishingStatus push(DataReference dataRef) throws DataProcessorException, DataOutputException {
    return PublishingStatus.EMPTY;
  }
  
  @Override
  public String toString() {
    return filter.toString();
  }
}
