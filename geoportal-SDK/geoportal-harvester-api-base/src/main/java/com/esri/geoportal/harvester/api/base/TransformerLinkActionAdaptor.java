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
import com.esri.geoportal.harvester.api.TransformerInstance;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.general.LinkAction;
import java.util.List;

/**
 * Transformer link action adaptor.
 */
public class TransformerLinkActionAdaptor implements LinkAction {
  private final TransformerInstance transformer;

  /**
   * Creates instance of the adaptor.
   * @param transformer transformer
   */
  public TransformerLinkActionAdaptor(TransformerInstance transformer) {
    this.transformer = transformer;
  }

  @Override
  public EntityDefinition getLinkActionDefinition() {
    return transformer.getTransformerDefinition();
  }

  @Override
  public List<DataReference> execute(DataReference dataRef) throws DataProcessorException, DataOutputException {
    return transformer.transform(dataRef);
  }
  
  @Override
  public String toString() {
    return transformer.toString();
  }
}
