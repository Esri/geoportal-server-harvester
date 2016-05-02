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

import java.io.IOException;
import java.util.Date;

/**
 * Data reference. It provides access to data itself as well as vital data 
 * information.
 * @param <T> type of data provided by the reference.
 */
public interface DataReference<T> {
  /**
   * Gets source uri.
   * @return source uri
   */
  String getSourceUri();
  
  /**
   * Gets last modified date.
   * @return last modified date or <code>null</code> if no date information available
   */
  Date getLastModifiedDate();
  
  /**
   * Gets content.
   * @return content
   * @throws IOException if getting content fails
   */
  T getContent() throws IOException;
}
