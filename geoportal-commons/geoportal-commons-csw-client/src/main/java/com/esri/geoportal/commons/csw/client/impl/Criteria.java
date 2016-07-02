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
package com.esri.geoportal.commons.csw.client.impl;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Operator;
import com.esri.geoportal.commons.csw.client.ICriteria;

/**
 * Criteria implementation.
 */
public class Criteria implements ICriteria {
  private int startPosition;
  private int maxRecords;
  private String searchText;
  private Envelope envelope;
  private Operator.Type operation;
  private boolean liveDataAndMapsOnly;

  @Override
  public int getStartPosition() {
    return startPosition;
  }

  /**
   * Sets start position.
   * @param startPosition start position 
   */
  public void setStartPosition(int startPosition) {
    this.startPosition = startPosition;
  }

  @Override
  public int getMaxRecords() {
    return maxRecords;
  }

  /**
   * Sets max records.
   * @param maxRecords max records 
   */
  public void setMaxRecords(int maxRecords) {
    this.maxRecords = maxRecords;
  }

  @Override
  public String getSearchText() {
    return searchText;
  }

  /**
   * Sets search text.
   * @param searchText search text 
   */
  public void setSearchText(String searchText) {
    this.searchText = searchText;
  }

  @Override
  public Envelope getEnvelope() {
    return envelope;
  }

  /**
   * Sets envelope.
   * @param envelope envelope
   */
  public void setEnvelope(Envelope envelope) {
    this.envelope = envelope;
  }

  @Override
  public Operator.Type getOperation() {
    return operation;
  }

  /**
   * Sets operation type.
   * @param operation operation type
   */
  public void setOperation(Operator.Type operation) {
    this.operation = operation;
  }

  @Override
  public boolean isLiveDataAndMapsOnly() {
    return liveDataAndMapsOnly;
  }

  /**
   * Sets to fetch live data and maps only.
   * @param liveDataAndMapsOnly <code>true</code> for live data and maps only
   */
  public void setLiveDataAndMapsOnly(boolean liveDataAndMapsOnly) {
    this.liveDataAndMapsOnly = liveDataAndMapsOnly;
  }
}
