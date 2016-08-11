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
package com.esri.geoportal.harvester.api;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.DataTransformerException;
import java.util.List;

/**
 * Transformer instance.
 * @see Transformer
 */
public interface TransformerInstance extends AutoCloseable {
  
  /**
   * Initialize instance.
   * @throws DataProcessorException if initialization fails.
   */
  void initialize() throws DataProcessorException;
  
  /**
   * Terminates instance
   * @throws DataProcessorException if termination fails.
   */
  void terminate() throws DataProcessorException;
  
  /**
   * Gets transformer definition.
   * @return transformer definition
   */
  EntityDefinition getTransformerDefinition();
  
  /**
   * Transforms data.
   * @param input input data
   * @return transformed data
   * @throws DataTransformerException if error transforming data
   */
  List<DataReference> transform(DataReference input) throws DataTransformerException;
}
