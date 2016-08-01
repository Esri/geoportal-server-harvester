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
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Data reference wrapper.
 */
public class DataReferenceWrapper implements DataReference {
  private final DataReference baseRef;
  private final byte [] content;
  
  /**
   * Creates instance of the data reference.
   * @param baseRef base data reference
   * @param content new content
   */
  public DataReferenceWrapper(DataReference baseRef, byte [] content) {
    this.baseRef = baseRef;
    this.content = content;
  }

  @Override
  public byte[] getContent() throws IOException {
    return content;
  }

  @Override
  public String getId() {
    return baseRef.getId();
  }

  @Override
  public Date getLastModifiedDate() {
    return baseRef.getLastModifiedDate();
  }

  @Override
  public URI getSourceUri() {
    return baseRef.getSourceUri();
  }

  @Override
  public URI getBrokerUri() {
    return baseRef.getBrokerUri();
  }

  @Override
  public Map<String, Object> getAttributesMap() {
    return baseRef.getAttributesMap();
  }

  @Override
  public DataReference getOriginDataReference() {
    return baseRef;
  }

  @Override
  public String toString() {
    return baseRef.toString();
  }
  
}
