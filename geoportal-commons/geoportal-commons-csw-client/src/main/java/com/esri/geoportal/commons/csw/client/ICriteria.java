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
package com.esri.geoportal.commons.csw.client;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Operator;

/**
 * Search criteria.
 */
public interface ICriteria {
  /**
   * Gets search start position.
   * @return start position
   */
  int getStartPosition();
  
  /**
   * Gets max records to return.
   * @return max records to return
   */
  int getMaxRecords();
  
  /**
   * Gets search text.
   * @return search text
   */
  String getSearchText();
  
  /**
   * Gets envelope.
   * @return envelope
   */
  Envelope getEnvelope();
  
  /**
   * Gets spatial operation type.
   * @return spatial operation type
   */
  Operator.Type getOperation();
  
  /**
   * Checks if live data and maps only expected
   * @return <code>true</code> if live data expected only
   */
  boolean isLiveDataAndMapsOnly();
}
