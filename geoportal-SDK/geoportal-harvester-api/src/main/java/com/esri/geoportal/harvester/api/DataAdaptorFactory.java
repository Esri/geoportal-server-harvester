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
package com.esri.geoportal.harvester.api;

import java.util.Map;

/**
 * Data adaptor factory.
 * @param <T> specific type of the adaptor
 */
public interface DataAdaptorFactory<T extends DataAdaptor> {
  /**
   * Creates instance of the adaptor.
   * @param attributes attributes used to initialize adaptor
   * @return instance of the adaptor
   * @throws IllegalArgumentException if incomplete or invalid attributes
   */
  T create(Map<String,String> attributes) throws IllegalArgumentException;
  
  /**
   * Gets adaptor template.
   * @return adaptor template
   */
  DataAdaptorTemplate getTemplate();
}
